package org.fg.ttrpg.setting.resource

import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.fg.ttrpg.common.dto.SettingDTO
import org.fg.ttrpg.common.dto.SettingObjectDTO

import org.fg.ttrpg.setting.*

import org.fg.ttrpg.account.GMRepository

import java.util.UUID

@Path("/api/settings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class SettingResource @Inject constructor(
    private val service: SettingService,
    private val objectRepo: SettingObjectRepository,
    private val templateRepo: TemplateRepository,
    private val gmRepo: GMRepository,
) {
    @GET
    fun list(): List<SettingDTO> =
        service.listAll().map { it.toDto() }

    @POST
    @Transactional
    fun create(dto: SettingDTO): SettingDTO {
        val gm = gmRepo.findById(dto.gmId) ?: throw NotFoundException()
        val entity = Setting().apply {
            name = dto.name
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
        val setting = service.findById(id) ?: throw NotFoundException()
        val template = dto.templateId?.let { templateRepo.findById(it) } ?: dto.templateId?.let { throw NotFoundException() }
        val obj = SettingObject().apply {
            slug = dto.slug
            name = dto.name
            description = dto.description
            payload = dto.payload
            tags = dto.tags.toMutableList()
            this.setting = setting
            this.template = template
        }
        objectRepo.persist(obj)
        return obj.toDto()
    }
}

private fun Setting.toDto() =
    SettingDTO(id, name ?: "", description, gm?.id ?: error("GM is null"))
private fun SettingObject.toDto() =
    SettingObjectDTO(
        id,
        slug ?: "",
        name ?: "",
        description,
        payload,
        tags.toList(),
        setting?.id ?: error("Setting is null"),
        template?.id
    )
