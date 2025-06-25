package org.fg.ttrpg.auth

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.util.UUID

@ApplicationScoped
class UserRoleRepository @Inject constructor(private val jdbi: Jdbi) {

    fun assign(userId: UUID, roleId: UUID) {
        jdbi.useHandle<Exception> { handle ->
            handle.createUpdate("INSERT INTO user_role (user_id, role_id) VALUES (:uid, :rid)")
                .bind("uid", userId)
                .bind("rid", roleId)
                .execute()
        }
    }

    fun remove(userId: UUID, roleId: UUID) {
        jdbi.useHandle<Exception> { handle ->
            handle.createUpdate("DELETE FROM user_role WHERE user_id = :uid AND role_id = :rid")
                .bind("uid", userId)
                .bind("rid", roleId)
                .execute()
        }
    }

    fun listRoles(userId: UUID): List<Role> =
        jdbi.withHandle<List<Role>, Exception> { handle ->
            handle.createQuery(
                "SELECT r.id, r.code FROM user_role ur JOIN role r ON ur.role_id = r.id WHERE ur.user_id = :uid"
            )
                .bind("uid", userId)
                .map(RoleMapper())
                .list()
        }

    private class RoleMapper : RowMapper<Role> {
        override fun map(rs: ResultSet, ctx: StatementContext): Role = Role().apply {
            id = rs.getObject("id", UUID::class.java)
            code = rs.getString("code")
        }
    }
}
