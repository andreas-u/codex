package org.fg.ttrpg.setting.resource

import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.jwt.JsonWebToken
import org.fg.ttrpg.common.dto.SettingDTO
import org.fg.ttrpg.common.dto.SettingObjectDTO
import org.fg.ttrpg.account.GMRepository
import org.fg.ttrpg.setting.Setting
import org.fg.ttrpg.setting.SettingObject
import org.fg.ttrpg.setting.SettingObjectRepository
import org.fg.ttrpg.setting.SettingService
import java.util.UUID

@Path("/api/settings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class SettingResource @Inject constructor(
    private val service: SettingService,
    private val objectRepo: SettingObjectRepository,
    private val gmRepo: GMRepository,
    private val jwt: JsonWebToken
) {
    private fun gmId() = UUID.fromString(jwt.getClaim("gmId"))

    @GET
    fun list(): List<SettingDTO> =
        service.listAll(gmId()).map { it.toDto() }

    @POST
    @Transactional
    fun create(dto: SettingDTO): SettingDTO {
        val gm = gmRepo.findById(gmId()) ?: throw NotFoundException()
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
        val setting = service.findByIdForGm(id, gmId()) ?: throw NotFoundException()
        val obj = SettingObject().apply {
            name = dto.name
            description = dto.description
            this.setting = setting
            this.gm = setting.gm
        }
        objectRepo.persist(obj)
        return obj.toDto()
    }
}

private fun Setting.toDto() = SettingDTO(id, name ?: "", description)
private fun SettingObject.toDto() =
    SettingObjectDTO(id, name ?: "", description, setting?.id ?: error("Setting is null"))
