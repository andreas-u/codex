package org.fg.ttrpg.setting.resource

import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import org.fg.ttrpg.common.dto.TemplateDTO
import org.fg.ttrpg.setting.Template
import org.fg.ttrpg.setting.TemplateRepository
import java.util.UUID

@Path("/api/templates")
@Produces(MediaType.APPLICATION_JSON)
class TemplateResource @Inject constructor(
    private val templates: TemplateRepository
) {
    @GET
    fun list(
        @QueryParam("settingId") settingId: UUID?,
        @QueryParam("genre") genre: String?,
        @QueryParam("type") type: String?
    ): List<TemplateDTO> {
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
        val list = if (filters.isEmpty()) {
            templates.listAll()
        } else {
            templates.list(filters.joinToString(" and "), *params.toTypedArray())
        }
        return list.map { it.toDto() }
    }
}

private fun Template.toDto() =
    TemplateDTO(id, name ?: "", description, schema, setting?.id ?: error("Setting is null"))
