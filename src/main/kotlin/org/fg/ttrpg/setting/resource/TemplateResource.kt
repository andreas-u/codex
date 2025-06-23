package org.fg.ttrpg.setting.resource

import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.jwt.JsonWebToken
import org.fg.ttrpg.common.dto.TemplateDTO
import org.fg.ttrpg.setting.Template
import org.fg.ttrpg.setting.TemplateRepository
import java.util.UUID

@Path("/api/templates")
@Produces(MediaType.APPLICATION_JSON)
class TemplateResource @Inject constructor(
    private val templates: TemplateRepository,
    private val jwt: JsonWebToken
) {
    private fun gmId() = UUID.fromString(jwt.getClaim("gmId"))
    @GET
    fun list(@QueryParam("settingId") settingId: UUID?): List<TemplateDTO> {
        val gid = gmId()
        val list = if (settingId != null) {
            templates.listBySettingAndGm(settingId, gid)
    fun list(
        @QueryParam("genre") genreId: UUID?,
        @QueryParam("type") type: String?
    ): List<TemplateDTO> {
        val list = if (genreId != null && type != null) {
            templates.listByGenreAndType(genreId, type)
        } else {
            templates.listByGm(gid)
        }
        return list.map { it.toDto() }
    }
}

private fun Template.toDto() =
    TemplateDTO(
        id,
        name ?: "",
        description,
        type ?: "",
        jsonSchema,
        genre?.id ?: error("Genre is null")
    )
