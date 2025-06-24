package org.fg.ttrpg.relationship

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.fg.ttrpg.campaign.Campaign
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class RelationshipOverrideRepository @Inject constructor(private val jdbi: Jdbi) {

    fun list(): List<RelationshipOverride> =
        jdbi.withHandle<List<RelationshipOverride>, Exception> { handle ->
            handle.createQuery(
                "SELECT id, campaign_id, base_relationship, override_mode, properties, created_at FROM relationship_override"
            )
                .map(RelationshipOverrideMapper())
                .list()
        }

    fun findById(id: UUID): RelationshipOverride? =
        jdbi.withHandle<RelationshipOverride?, Exception> { handle ->
            handle.createQuery(
                "SELECT id, campaign_id, base_relationship, override_mode, properties, created_at FROM relationship_override WHERE id = :id"
            )
                .bind("id", id)
                .map(RelationshipOverrideMapper())
                .findOne()
                .orElse(null)
        }

    fun persist(override: RelationshipOverride) {
        jdbi.useHandle<Exception> { handle ->
            handle.createUpdate(
                "INSERT INTO relationship_override (id, campaign_id, base_relationship, override_mode, properties, created_at) " +
                    "VALUES (:id, :campaignId, :baseRelationship, :overrideMode, :properties::jsonb, :createdAt)"
            )
                .bind("id", override.id)
                .bind("campaignId", override.campaign?.id)
                .bind("baseRelationship", override.baseRelationship?.id)
                .bind("overrideMode", override.overrideMode?.name)
                .bind("properties", override.properties)
                .bind("createdAt", override.createdAt)
                .execute()
        }
    }

    fun update(override: RelationshipOverride) {
        jdbi.useHandle<Exception> { handle ->
            handle.createUpdate(
                """
                UPDATE relationship_override SET
                    campaign_id = :campaignId,
                    base_relationship = :baseRelationship,
                    override_mode = :overrideMode,
                    properties = :properties::jsonb
                WHERE id = :id
                """
            )
                .bind("id", override.id)
                .bind("campaignId", override.campaign?.id)
                .bind("baseRelationship", override.baseRelationship?.id)
                .bind("overrideMode", override.overrideMode?.name)
                .bind("properties", override.properties)
                .execute()
        }
    }

    fun deleteById(id: UUID) {
        jdbi.useHandle<Exception> { handle ->
            handle.createUpdate("DELETE FROM relationship_override WHERE id = :id")
                .bind("id", id)
                .execute()
        }
    }

    private class RelationshipOverrideMapper : RowMapper<RelationshipOverride> {
        override fun map(rs: ResultSet, ctx: StatementContext): RelationshipOverride =
            RelationshipOverride().apply {
                id = rs.getObject("id", UUID::class.java)
                campaign = Campaign().apply { id = rs.getObject("campaign_id", UUID::class.java) }
                val baseId = rs.getObject("base_relationship", UUID::class.java)
                if (baseId != null) {
                    baseRelationship = Relationship().apply { id = baseId }
                }
                overrideMode = OverrideMode.valueOf(rs.getString("override_mode"))
                properties = rs.getString("properties")
                createdAt = rs.getTimestamp("created_at").toInstant()
            }
    }
}
