package org.fg.ttrpg.campaign

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.fg.ttrpg.timeline.TimelineEvent
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.util.UUID

@ApplicationScoped
class CampaignEventOverrideRepository @Inject constructor(private val jdbi: Jdbi) {

    fun list(): List<CampaignEventOverride> =
        jdbi.withHandle<List<CampaignEventOverride>, Exception> { handle ->
            handle.createQuery(
                "SELECT id, campaign_id, base_event_id, override_mode, payload, created_at FROM campaign_event_override"
            )
                .map(CampaignEventOverrideMapper())
                .list()
        }

    fun findById(id: UUID): CampaignEventOverride? =
        jdbi.withHandle<CampaignEventOverride?, Exception> { handle ->
            handle.createQuery(
                "SELECT id, campaign_id, base_event_id, override_mode, payload, created_at FROM campaign_event_override WHERE id = :id"
            )
                .bind("id", id)
                .map(CampaignEventOverrideMapper())
                .findOne()
                .orElse(null)
        }

    fun persist(override: CampaignEventOverride): Int? {
        return jdbi.withHandle<Int,Exception> { handle ->
            handle.createUpdate(
                "INSERT INTO campaign_event_override (id, campaign_id, base_event_id, override_mode, payload, created_at) VALUES (:id, :campaignId, :baseEventId, :overrideMode, :payload::jsonb, :createdAt)"
            )
                .bind("id", override.id)
                .bind("campaignId", override.campaign?.id)
                .bind("baseEventId", override.baseEvent?.id)
                .bind("overrideMode", override.overrideMode?.name)
                .bind("payload", override.payload)
                .bind("createdAt", override.createdAt)
                .execute()
        }
    }

    fun update(override: CampaignEventOverride) {
        jdbi.useHandle<Exception> { handle ->
            handle.createUpdate(
                """
                UPDATE campaign_event_override SET
                    campaign_id = :campaignId,
                    base_event_id = :baseEventId,
                    override_mode = :overrideMode,
                    payload = :payload::jsonb
                WHERE id = :id
                """
            )
                .bind("id", override.id)
                .bind("campaignId", override.campaign?.id)
                .bind("baseEventId", override.baseEvent?.id)
                .bind("overrideMode", override.overrideMode?.name)
                .bind("payload", override.payload)
                .execute()
        }
    }

    fun deleteById(id: UUID) {
        jdbi.useHandle<Exception> { handle ->
            handle.createUpdate("DELETE FROM campaign_event_override WHERE id = :id")
                .bind("id", id)
                .execute()
        }
    }

    fun listByCampaign(campaignId: UUID): List<CampaignEventOverride> =
        jdbi.withHandle<List<CampaignEventOverride>, Exception> { handle ->
            handle.createQuery(
                "SELECT id, campaign_id, base_event_id, override_mode, payload, created_at FROM campaign_event_override WHERE campaign_id = :cid"
            )
                .bind("cid", campaignId)
                .map(CampaignEventOverrideMapper())
                .list()
        }

    fun findByCampaignAndEvent(campaignId: UUID, eventId: UUID): CampaignEventOverride? =
        jdbi.withHandle<CampaignEventOverride?, Exception> { handle ->
            handle.createQuery(
                "SELECT id, campaign_id, base_event_id, override_mode, payload, created_at FROM campaign_event_override WHERE campaign_id = :cid AND base_event_id = :eid"
            )
                .bind("cid", campaignId)
                .bind("eid", eventId)
                .map(CampaignEventOverrideMapper())
                .findOne()
                .orElse(null)
        }

    private class CampaignEventOverrideMapper : RowMapper<CampaignEventOverride> {
        override fun map(rs: ResultSet, ctx: StatementContext): CampaignEventOverride = CampaignEventOverride().apply {
            id = rs.getObject("id", UUID::class.java)
            campaign = Campaign().apply { id = rs.getObject("campaign_id", UUID::class.java) }
            val baseId = rs.getObject("base_event_id", UUID::class.java)
            if (baseId != null) {
                baseEvent = TimelineEvent().apply { id = baseId }
            }
            overrideMode = OverrideMode.valueOf(rs.getString("override_mode"))
            payload = rs.getString("payload")
            createdAt = rs.getTimestamp("created_at").toInstant()
        }
    }
}
