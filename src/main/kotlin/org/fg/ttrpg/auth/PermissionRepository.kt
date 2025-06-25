package org.fg.ttrpg.auth

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.util.UUID

@ApplicationScoped
class PermissionRepository @Inject constructor(private val jdbi: Jdbi) {

    fun findById(id: UUID): Permission? =
        jdbi.withHandle<Permission?, Exception> { handle ->
            handle.createQuery("SELECT id, code FROM permission WHERE id = :id")
                .bind("id", id)
                .map(PermissionMapper())
                .findOne()
                .orElse(null)
        }

    fun persist(permission: Permission) {
        jdbi.useHandle<Exception> { handle ->
            handle.createUpdate("INSERT INTO permission (id, code) VALUES (:id, :code)")
                .bind("id", permission.id)
                .bind("code", permission.code)
                .execute()
        }
    }

    private class PermissionMapper : RowMapper<Permission> {
        override fun map(rs: ResultSet, ctx: StatementContext): Permission = Permission().apply {
            id = rs.getObject("id", UUID::class.java)
            code = rs.getString("code")
        }
    }
}
