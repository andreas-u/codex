package org.fg.ttrpg.auth

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jdbi.v3.core.Jdbi

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
}
