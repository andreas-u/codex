package org.fg.ttrpg.auth

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.fg.ttrpg.account.GM
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.util.UUID

@ApplicationScoped
class UserRepository @Inject constructor(private val jdbi: Jdbi) {

    fun findById(id: UUID): User? =
        jdbi.withHandle<User?, Exception> { handle ->
            handle.createQuery("SELECT id, username, email, gm_id FROM \"user\" WHERE id = :id")
                .bind("id", id)
                .map(UserMapper())
                .findOne()
                .orElse(null)
        }

    fun findByGmId(gmId: UUID): User? =
        jdbi.withHandle<User?, Exception> { handle ->
            handle.createQuery("SELECT id, username, email, gm_id FROM \"user\" WHERE gm_id = :gmId LIMIT 1")
                .bind("gmId", gmId)
                .map(UserMapper())
                .findOne()
                .orElse(null)
        }

    fun persist(user: User) {
        jdbi.useHandle<Exception> { handle ->
            handle.createUpdate("INSERT INTO \"user\" (id, username, email, gm_id) VALUES (:id, :username, :email, :gmId)")
                .bind("id", user.id)
                .bind("username", user.username)
                .bind("email", user.email)
                .bind("gmId", user.gm?.id)
                .execute()
        }
    }

    private class UserMapper : RowMapper<User> {
        override fun map(rs: ResultSet, ctx: StatementContext): User = User().apply {
            id = rs.getObject("id", UUID::class.java)
            username = rs.getString("username")
            email = rs.getString("email")
            gm = GM().apply { id = rs.getObject("gm_id", UUID::class.java) }
        }
    }
}
