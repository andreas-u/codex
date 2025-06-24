package org.fg.ttrpg.relationship

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.fg.ttrpg.setting.Setting
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class RelationshipTypeRepository @Inject constructor(private val jdbi: Jdbi) {

    fun list(): List<RelationshipType> =
        jdbi.withHandle<List<RelationshipType>, Exception> { handle ->
            handle.createQuery(
                "SELECT id, setting_id, code, display_name, directional, schema_json, created_at FROM relationship_type"
            )
                .map(RelationshipTypeMapper())
                .list()
        }

    fun listBySetting(settingId: UUID): List<RelationshipType> =
        jdbi.withHandle<List<RelationshipType>, Exception> { handle ->
            handle.createQuery(
                "SELECT id, setting_id, code, display_name, directional, schema_json, created_at FROM relationship_type WHERE setting_id = :settingId"
            )
                .bind("settingId", settingId)
                .map(RelationshipTypeMapper())
                .list()
        }

    fun findById(id: UUID): RelationshipType? =
        jdbi.withHandle<RelationshipType?, Exception> { handle ->
            handle.createQuery(
                "SELECT id, setting_id, code, display_name, directional, schema_json, created_at FROM relationship_type WHERE id = :id"
            )
                .bind("id", id)
                .map(RelationshipTypeMapper())
                .findOne()
                .orElse(null)
        }

    fun persist(type: RelationshipType) {
        jdbi.useHandle<Exception> { handle ->
            handle.createUpdate(
                "INSERT INTO relationship_type (id, setting_id, code, display_name, directional, schema_json, created_at) " +
                    "VALUES (:id, :settingId, :code, :displayName, :directional, :schemaJson::jsonb, :createdAt)"
            )
                .bind("id", type.id)
                .bind("settingId", type.setting?.id)
                .bind("code", type.code)
                .bind("displayName", type.displayName)
                .bind("directional", type.directional)
                .bind("schemaJson", type.schemaJson)
                .bind("createdAt", type.createdAt)
                .execute()
        }
    }

    fun update(type: RelationshipType) {
        jdbi.useHandle<Exception> { handle ->
            handle.createUpdate(
                """
                UPDATE relationship_type SET
                    setting_id = :settingId,
                    code = :code,
                    display_name = :displayName,
                    directional = :directional,
                    schema_json = :schemaJson::jsonb
                WHERE id = :id
                """
            )
                .bind("id", type.id)
                .bind("settingId", type.setting?.id)
                .bind("code", type.code)
                .bind("displayName", type.displayName)
                .bind("directional", type.directional)
                .bind("schemaJson", type.schemaJson)
                .execute()
        }
    }

    fun deleteById(id: UUID) {
        jdbi.useHandle<Exception> { handle ->
            handle.createUpdate("DELETE FROM relationship_type WHERE id = :id")
                .bind("id", id)
                .execute()
        }
    }

    private class RelationshipTypeMapper : RowMapper<RelationshipType> {
        override fun map(rs: ResultSet, ctx: StatementContext): RelationshipType =
            RelationshipType().apply {
                id = rs.getObject("id", UUID::class.java)
                setting = Setting().apply { id = rs.getObject("setting_id", UUID::class.java) }
                code = rs.getString("code")
                displayName = rs.getString("display_name")
                directional = rs.getBoolean("directional")
                schemaJson = rs.getString("schema_json")
                createdAt = rs.getTimestamp("created_at").toInstant()
            }
    }
}
