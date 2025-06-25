package org.fg.ttrpg.auth

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jdbi.v3.core.Jdbi
import java.util.UUID

@ApplicationScoped
class ObjectGrantRepository @Inject constructor(private val jdbi: Jdbi) {

    fun persist(grant: ObjectGrant) {
        jdbi.useHandle<Exception> { handle ->
            handle.createUpdate(
                "INSERT INTO object_grant (id, user_id, object_id, permission_id, granted_by, grant_time) " +
                    "VALUES (:id, :userId, :objectId, :permissionId, :grantedBy, :grantTime)"
            )
                .bind("id", grant.id)
                .bind("userId", grant.user?.id)
                .bind("objectId", grant.objectId)
                .bind("permissionId", grant.permission?.id)
                .bind("grantedBy", grant.grantedBy?.id)
                .bind("grantTime", grant.grantTime)
                .execute()
        }
    }

    fun hasPermission(userId: UUID, objectId: UUID, code: String): Boolean =
        jdbi.withHandle<Boolean, Exception> { handle ->
            handle.createQuery(
                """
                SELECT COUNT(*) FROM object_grant og
                JOIN permission p ON og.permission_id = p.id
                WHERE og.user_id = :userId AND og.object_id = :objectId AND p.code = :code
                """
            )
                .bind("userId", userId)
                .bind("objectId", objectId)
                .bind("code", code)
                .mapTo(Int::class.java)
                .one() > 0
        }
}
