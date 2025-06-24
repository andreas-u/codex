package org.fg.ttrpg.relationship

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.fg.ttrpg.setting.Setting
import org.fg.ttrpg.setting.SettingObject
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class RelationshipRepository @Inject constructor(private val jdbi: Jdbi) {

    fun listBySetting(settingId: UUID): List<Relationship> =
        jdbi.withHandle<List<Relationship>, Exception> { handle ->
            handle.createQuery("SELECT id, setting_id, type_id, source_object, target_object, is_bidirectional, properties, created_at FROM relationship WHERE setting_id = :settingId")
                .bind("settingId", settingId)
                .map(RelationshipMapper())
                .list()
        }

    private class RelationshipMapper : RowMapper<Relationship> {
        override fun map(rs: ResultSet, ctx: StatementContext): Relationship = Relationship().apply {
            id = rs.getObject("id", UUID::class.java)
            setting = Setting().apply { id = rs.getObject("setting_id", UUID::class.java) }
            type = RelationshipType().apply { id = rs.getObject("type_id", UUID::class.java) }
            sourceObject = SettingObject().apply { id = rs.getObject("source_object", UUID::class.java) }
            targetObject = SettingObject().apply { id = rs.getObject("target_object", UUID::class.java) }
            isBidirectional = rs.getBoolean("is_bidirectional")
            properties = rs.getString("properties")
            createdAt = rs.getTimestamp("created_at").toInstant()
        }
    }
}
