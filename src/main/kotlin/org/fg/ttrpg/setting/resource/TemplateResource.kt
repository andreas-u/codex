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
    fun list(
        @QueryParam("genre") genreId: UUID?,
        @QueryParam("type") type: String?
    ): List<TemplateDTO> {
        val gid = gmId()
        val list = when {
            genreId != null && type == null -> templates.listByGenre(genreId.toString())
            type != null && genreId == null -> templates.listByType(type)
            else -> templates.listByGm(gid)
        }
        return list.map { it.toDto() }
    }
}

private fun Template.toDto() =
    TemplateDTO(
        id,
        title ?: "",
        description,
        type ?: "",
        jsonSchema,
        genre?.id ?: error("Genre is null")
    )
