package org.fg.ttrpg.account

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.util.UUID

@ApplicationScoped
class GMRepository @Inject constructor(private val jdbi: Jdbi) {

    fun findById(id: UUID): GM? =
        jdbi.withHandle<GM?, Exception> { handle ->
            handle.createQuery("SELECT id, username, email FROM gm WHERE id = :id")
                .bind("id", id)
                .map(GMMapper())
                .findOne()
                .orElse(null)
        }

    fun persist(gm: GM) {
        jdbi.useHandle<Exception> { handle ->
            handle.createUpdate("INSERT INTO gm (id, username, email) VALUES (:id, :username, :email)")
                .bind("id", gm.id)
                .bind("username", gm.username)
                .bind("email", gm.email)
                .execute()
        }
    }

    fun deleteById(gmId: java.util.UUID) {
        jdbi.useHandle<Exception> { handle ->
            handle.createUpdate("DELETE FROM gm WHERE id = :id")
                .bind("id", gmId)
                .execute()
        }
    }

    private class GMMapper : RowMapper<GM> {
        override fun map(rs: ResultSet, ctx: StatementContext): GM = GM().apply {
            id = rs.getObject("id", UUID::class.java)
            username = rs.getString("username")
            email = rs.getString("email")
        }
    }
}
