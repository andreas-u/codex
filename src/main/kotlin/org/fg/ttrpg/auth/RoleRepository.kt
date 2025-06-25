package org.fg.ttrpg.auth

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.util.UUID

@ApplicationScoped
class RoleRepository @Inject constructor(private val jdbi: Jdbi) {

    fun findById(id: UUID): Role? =
        jdbi.withHandle<Role?, Exception> { handle ->
            handle.createQuery("SELECT id, code FROM role WHERE id = :id")
                .bind("id", id)
                .map(RoleMapper())
                .findOne()
                .orElse(null)
        }

    fun persist(role: Role) {
        jdbi.useHandle<Exception> { handle ->
            handle.createUpdate("INSERT INTO role (id, code) VALUES (:id, :code)")
                .bind("id", role.id)
                .bind("code", role.code)
                .execute()
        }
    }

    private class RoleMapper : RowMapper<Role> {
        override fun map(rs: ResultSet, ctx: StatementContext): Role = Role().apply {
            id = rs.getObject("id", UUID::class.java)
            code = rs.getString("code")
        }
    }
}
