package org.fg.ttrpg.auth.resource

import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.fg.ttrpg.auth.*
import org.fg.ttrpg.common.dto.GrantDTO
import java.time.Instant
import java.util.*

@Path("/api/grants")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class GrantResource @Inject constructor(
    private val userRepo: UserRepository,
    private val permissionRepo: PermissionRepository,
    private val grantRepo: ObjectGrantRepository,
    private val auth: AuthorizationService
) {

    @POST
    @Transactional
    fun create(dto: GrantDTO): GrantDTO {
        val user = userRepo.findById(dto.userId) ?: throw NotFoundException()
        val perm = permissionRepo.findByCode(dto.permissionCode) ?: throw NotFoundException()
        val granter = userRepo.findById(dto.grantedBy) ?: throw NotFoundException()
        val grant = ObjectGrant().apply {
            id = UUID.randomUUID()
            this.user = user
            objectId = dto.objectId
            permission = perm
            grantedBy = granter
            grantTime = Instant.now()
        }
        grantRepo.persist(grant)
        return grant.toDto()
    }

    @GET
    @Path("check")
    fun check(
        @QueryParam("userId") userId: UUID,
        @QueryParam("objectId") objectId: UUID,
        @QueryParam("code") code: String
    ): Boolean = auth.hasPermission(userId, objectId, code)
}

private fun ObjectGrant.toDto() = GrantDTO(id, user?.id!!, objectId!!, permission?.code ?: "", grantedBy?.id!!)
