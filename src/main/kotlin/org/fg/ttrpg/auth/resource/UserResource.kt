package org.fg.ttrpg.auth.resource

import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.fg.ttrpg.auth.*
import org.fg.ttrpg.common.dto.RoleDTO
import java.util.*

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class UserResource @Inject constructor(
    private val userRepo: UserRepository,
    private val roleRepo: RoleRepository,
    private val userRoleRepo: UserRoleRepository
) {

    @GET
    @Path("{id}/roles")
    fun roles(@PathParam("id") id: UUID): List<RoleDTO> =
        userRoleRepo.listRoles(id).map { it.toDto() }

    @POST
    @Path("{id}/roles/{rid}")
    @Transactional
    fun assignRole(
        @PathParam("id") id: UUID,
        @PathParam("rid") rid: UUID
    ) {
        userRepo.findById(id) ?: throw NotFoundException()
        roleRepo.findById(rid) ?: throw NotFoundException()
        userRoleRepo.assign(id, rid)
    }

    @DELETE
    @Path("{id}/roles/{rid}")
    @Transactional
    fun removeRole(
        @PathParam("id") id: UUID,
        @PathParam("rid") rid: UUID
    ) {
        userRoleRepo.remove(id, rid)
    }
}

private fun Role.toDto() = RoleDTO(id, code ?: "")
