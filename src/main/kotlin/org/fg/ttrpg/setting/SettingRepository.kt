package org.fg.ttrpg.setting

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.fg.ttrpg.account.GM
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class SettingRepository @Inject constructor(private val jdbi: Jdbi) {

    fun listByGm(gmId: UUID): List<Setting> =
        jdbi.withHandle<List<Setting>, Exception> { handle ->
            handle.createQuery("SELECT id, name, description, gm_id, created_at FROM setting WHERE gm_id = :gmId")
                .bind("gmId", gmId)
                .map(SettingMapper())
                .list()
        }

    fun findById(id: UUID): Setting? =
        jdbi.withHandle<Setting?, Exception> { handle ->
            handle.createQuery("SELECT id, name, description, gm_id, created_at FROM setting WHERE id = :id")
                .bind("id", id)
                .map(SettingMapper())
                .findOne()
                .orElse(null)
        }

    fun findByIdForGm(id: UUID, gmId: UUID): Setting? =
        jdbi.withHandle<Setting?, Exception> { handle ->
            handle.createQuery("SELECT id, name, description, gm_id, created_at FROM setting WHERE id = :id AND gm_id = :gmId")
                .bind("id", id)
                .bind("gmId", gmId)
                .map(SettingMapper())
                .findOne()
                .orElse(null)
        }

    fun persist(setting: Setting) {
        if (setting.id == null) {
            setting.id = java.util.UUID.randomUUID()
        }
        jdbi.useHandle<Exception> { handle ->
            if (setting.createdAt != null) {
                handle.createUpdate("INSERT INTO setting (id, name, description, gm_id, created_at) VALUES (:id, :name, :description, :gmId, :createdAt)")
                    .bind("id", setting.id)
                    .bind("name", setting.title)
                    .bind("description", setting.description)
                    .bind("gmId", setting.gm?.id)
                    .bind("createdAt", setting.createdAt)
                    .execute()
            } else {
                handle.createUpdate("INSERT INTO setting (id, name, description, gm_id) VALUES (:id, :name, :description, :gmId)")
                    .bind("id", setting.id)
                    .bind("name", setting.title)
                    .bind("description", setting.description)
                    .bind("gmId", setting.gm?.id)
                    .execute()
            }
        }
    }

    private class SettingMapper : RowMapper<Setting> {
        override fun map(rs: ResultSet, ctx: StatementContext): Setting = Setting().apply {
            id = rs.getObject("id", UUID::class.java)
            title = rs.getString("name")
            description = rs.getString("description")
            createdAt = rs.getTimestamp("created_at").toInstant()
            gm = GM().apply { id = rs.getObject("gm_id", UUID::class.java) }
        }
    }
}
