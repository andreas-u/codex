package org.fg.ttrpg.auth.resource

import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.fg.ttrpg.auth.Role
import org.fg.ttrpg.auth.RoleRepository
import org.fg.ttrpg.common.dto.RoleDTO
import java.util.*

@Path("/api/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class RoleResource @Inject constructor(private val repo: RoleRepository) {

    @GET
    fun list(): List<RoleDTO> = repo.listAll().map { it.toDto() }

    @GET
    @Path("{id}")
    fun get(@PathParam("id") id: UUID): RoleDTO {
        return repo.findById(id)?.toDto() ?: throw NotFoundException()
    }

    @POST
    @Transactional
    fun create(dto: RoleDTO): RoleDTO {
        val role = Role().apply {
            id = UUID.randomUUID()
            code = dto.code
        }
        repo.persist(role)
        return role.toDto()
    }
}

private fun Role.toDto() = RoleDTO(id, code ?: "")
