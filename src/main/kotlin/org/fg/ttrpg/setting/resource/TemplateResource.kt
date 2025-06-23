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
        @QueryParam("genre") genreId: UUID?,
        @QueryParam("type") type: String?
    ): List<TemplateDTO> {
        val list = if (genreId != null && type != null) {
            templates.listByGenreAndType(genreId, type)
        } else {
            templates.listAll()
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
