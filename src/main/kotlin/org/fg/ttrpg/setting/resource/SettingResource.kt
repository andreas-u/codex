package org.fg.ttrpg.setting.resource

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.jwt.JsonWebToken
import org.fg.ttrpg.account.GMRepository
import org.fg.ttrpg.common.dto.SettingDTO
import org.fg.ttrpg.common.dto.SettingObjectDTO
import org.fg.ttrpg.setting.*
import java.util.UUID

@Path("/api/settings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class SettingResource @Inject constructor(
    private val service: SettingService,
    private val objectRepo: SettingObjectRepository,
    private val templateRepo: TemplateRepository,
    private val validator: org.fg.ttrpg.infra.validation.TemplateValidator,
    private val gmRepo: GMRepository,
    private val jwt: JsonWebToken
) {
    private val mapper = ObjectMapper()
    private fun gmId() = UUID.fromString(jwt.getClaim("gmId"))

    @GET
    fun list(): List<SettingDTO> =
        service.listAll(gmId()).map { it.toDto() }

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
        return entity.toDto()
    }

    @POST
    @Path("{id}/objects")
    @Transactional
    fun createObject(@PathParam("id") id: UUID, dto: SettingObjectDTO): SettingObjectDTO {
        val setting = service.findByIdForGm(id, gmId()) ?: throw NotFoundException()
        val template = dto.templateId?.let { templateRepo.findById(it) } ?: dto.templateId?.let { throw NotFoundException() }
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
        return obj.toDto()
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
