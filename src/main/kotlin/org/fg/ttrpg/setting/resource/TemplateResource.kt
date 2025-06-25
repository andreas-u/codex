package org.fg.ttrpg.setting.resource

import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.NotFoundException
import org.eclipse.microprofile.jwt.JsonWebToken
import org.fg.ttrpg.common.dto.TemplateDTO
import org.fg.ttrpg.setting.Template
import org.fg.ttrpg.setting.TemplateService
import org.fg.ttrpg.auth.UserRepository
import java.util.UUID

@Path("/api/templates")
@Produces(MediaType.APPLICATION_JSON)
class TemplateResource @Inject constructor(
    private val service: TemplateService,
    private val jwt: JsonWebToken,
    private val userRepo: UserRepository
) {
    private fun gmId() = userRepo.findById(UUID.fromString(jwt.getClaim("userId")))?.gm?.id
        ?: throw NotFoundException()

    @GET
    fun list(
        @QueryParam("genre") genreId: UUID?,
        @QueryParam("type") type: String?
    ): List<TemplateDTO> {
        val gid = gmId()
        val list = when {
            genreId != null && type == null -> service.listByGenre(genreId.toString())
            type != null && genreId == null -> service.listByType(type)
            else -> service.listAll(gid)
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
