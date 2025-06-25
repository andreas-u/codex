package org.fg.ttrpg.auth

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.fg.ttrpg.auth.UserRepository
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class AuthorizationService @Inject constructor(
    private val grantRepository: ObjectGrantRepository,
    private val permissionRepository: PermissionRepository,
    private val userRepository: UserRepository,
) {
    fun hasPermission(userId: UUID, objectId: UUID, permissionCode: String): Boolean {
        return grantRepository.hasPermission(userId, objectId, permissionCode)
    }

    /** Grant "FULL" permission on the object to the user. */
    fun grantFullPermission(userId: UUID, objectId: UUID, grantedBy: UUID) {
        val permission = permissionRepository.findByCode("FULL") ?: return
        val user = userRepository.findById(userId) ?: return
        val granter = userRepository.findById(grantedBy) ?: return
        val grant = ObjectGrant().apply {
            id = UUID.randomUUID()
            this.user = user
            this.permission = permission
            this.objectId = objectId
            this.grantedBy = granter
            grantTime = Instant.now()
        }
        grantRepository.persist(grant)
    }
}
