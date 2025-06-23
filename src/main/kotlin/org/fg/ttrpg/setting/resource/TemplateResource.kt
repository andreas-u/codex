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
    fun list(
        @QueryParam("settingId") settingId: UUID?,
        @QueryParam("genre") genre: String?,
        @QueryParam("type") type: String?
    ): List<TemplateDTO> {
        val list = when {
            settingId != null && genre == null && type == null ->
                templates.list("setting.id", settingId)
            settingId == null && genre != null && type == null ->
                templates.listByGenre(genre)
            settingId == null && genre == null && type != null ->
                templates.listByType(type)
            settingId == null && genre == null && type == null ->
                templates.listAll()
            else -> {
                val filters = mutableListOf<String>()
                val params = mutableListOf<Any>()
                if (settingId != null) {
                    filters.add("setting.id = ?${'$'}{filters.size + 1}")
                    params.add(settingId)
                }
                if (genre != null) {
                    filters.add("setting.genres.name = ?${'$'}{filters.size + 1}")
                    params.add(genre)
                }
                if (type != null) {
                    filters.add("name = ?${'$'}{filters.size + 1}")
                    params.add(type)
                }
                templates.list(filters.joinToString(" and "), *params.toTypedArray())
            }
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
