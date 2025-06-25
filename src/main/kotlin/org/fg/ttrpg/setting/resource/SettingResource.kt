package org.fg.ttrpg.setting.resource

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.jwt.JsonWebToken
import org.fg.ttrpg.account.GMRepository
import org.fg.ttrpg.auth.AuthorizationService
import org.fg.ttrpg.auth.UserRepository
import org.fg.ttrpg.calendar.CalendarService
import org.fg.ttrpg.calendar.CalendarSystem
import org.fg.ttrpg.calendar.resource.toDto
import org.fg.ttrpg.common.dto.CalendarDTO
import org.fg.ttrpg.common.dto.SettingDTO
import org.fg.ttrpg.common.dto.SettingObjectDTO
import org.fg.ttrpg.setting.*
import java.util.*

@Path("/api/settings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class SettingResource @Inject constructor(
    private val service: SettingService,
    private val objectRepo: SettingObjectRepository,
    private val templateRepo: TemplateRepository,
    private val validator: org.fg.ttrpg.infra.validation.TemplateValidator,
    private val gmRepo: GMRepository,
    private val userRepo: UserRepository,
    private val calendarService: CalendarService,
    private val auth: AuthorizationService,
    private val jwt: JsonWebToken
) {
    private val mapper = ObjectMapper()
    private fun userId() = UUID.fromString(jwt.getClaim("userId"))
    private fun gmId(): UUID =
        userRepo.findById(userId())?.gm?.id ?: throw NotFoundException()

    @GET
    fun list(): List<SettingDTO> =
        service.listAll(gmId()).map { it.toDto() }

    @GET
    @Path("{id}")
    fun getById(@PathParam("id") id: UUID): SettingDTO {
        val setting = service.findByIdForGm(id, gmId()) ?: throw NotFoundException()
        return setting.toDto()
    }

    @POST
    @Transactional
    fun create(dto: SettingDTO): SettingDTO {
        val gm = gmRepo.findById(gmId()) ?: throw NotFoundException()
        val entity = Setting().apply {
            title = dto.title
            description = dto.description
            this.gm = gm
        }
        service.persist(entity)
        entity.id?.let { auth.grantFullPermission(userId(), it, userId()) }
        return entity.toDto()
    }

    @POST
    @Path("{id}/objects")
    @Transactional
    fun createObject(@PathParam("id") id: UUID, dto: SettingObjectDTO): SettingObjectDTO {
        val setting = service.findByIdForGm(id, gmId()) ?: throw NotFoundException()
        val template = dto.templateId?.let { templateRepo.findByIdForGm(it, gmId()) }
            ?: dto.templateId?.let { throw NotFoundException() }
        val obj = SettingObject().apply {
            slug = dto.slug
            title = dto.title
            description = dto.description
            payload = dto.payload
            tags = dto.tags.toMutableList()
            this.setting = setting
            this.template = template
            this.gm = setting.gm
        }
        template?.id?.let { tid ->
            val node = mapper.readTree(dto.payload ?: "{}")
            try {
                validator.validate(tid, node)
            } catch (e: org.fg.ttrpg.infra.validation.TemplateValidationException) {
                throw WebApplicationException(e.message, 422)
            }
        }
        objectRepo.persist(obj)
        obj.id?.let { auth.grantFullPermission(userId(), it, userId()) }
        return obj.toDto()
    }


    @POST
    @Path("{id}/calendars")
    @Transactional
    fun createCalendar(@PathParam("id") settingId: UUID, dto: CalendarDTO): CalendarDTO {
        val setting = service.findByIdForGm(settingId, gmId()) ?: throw NotFoundException()
        val system = CalendarSystem().apply {
            name = dto.name
            epochLabel = dto.epochLabel
            months = dto.months
            leapRule = dto.leapRule
            this.setting = setting
        }
        calendarService.persist(system)
        system.id?.let { auth.grantFullPermission(userId(), it, userId()) }
        return system.toDto()
    }
}

private fun Setting.toDto() =
    SettingDTO(id, title ?: "", description, gm?.id ?: error("GM is null"))

private fun SettingObject.toDto() =
    SettingObjectDTO(
        id,
        slug ?: "",
        title ?: "",
        description,
        payload,
        tags.toList(),
        setting?.id ?: error("Setting is null"),
        template?.id
    )
