package org.fg.ttrpg.campaign

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.fg.ttrpg.account.GM
import org.fg.ttrpg.setting.Setting
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class CampaignRepository @Inject constructor(private val jdbi: Jdbi) {

    fun listByGm(gmId: UUID): List<Campaign> =
        jdbi.withHandle<List<Campaign>, Exception> { handle ->
            handle.createQuery("SELECT id, title, status, started_on, gm_id, setting_id FROM campaign WHERE gm_id = :gmId")
                .bind("gmId", gmId)
                .map(CampaignMapper())
                .list()
        }

    fun findById(id: UUID): Campaign? =
        jdbi.withHandle<Campaign?, Exception> { handle ->
            handle.createQuery("SELECT id, title, status, started_on, gm_id, setting_id FROM campaign WHERE id = :id")
                .bind("id", id)
                .map(CampaignMapper())
                .findOne()
                .orElse(null)
        }

    fun findByIdForGm(id: UUID, gmId: UUID): Campaign? =
        jdbi.withHandle<Campaign?, Exception> { handle ->
            handle.createQuery("SELECT id, title, status, started_on, gm_id, setting_id FROM campaign WHERE id = :id AND gm_id = :gmId")
                .bind("id", id)
                .bind("gmId", gmId)
                .map(CampaignMapper())
                .findOne()
                .orElse(null)
        }

    fun persist(campaign: Campaign) {
        jdbi.useHandle<Exception> { handle ->
            handle.createUpdate("INSERT INTO campaign (id, title, status, started_on, gm_id, setting_id) VALUES (:id, :title, :status, :startedOn, :gmId, :settingId)")
                .bind("id", campaign.id)
                .bind("title", campaign.title)
                .bind("status", campaign.status?.name)
                .bind("startedOn", campaign.startedOn)
                .bind("gmId", campaign.gm?.id)
                .bind("settingId", campaign.setting?.id)
                .execute()
        }
    }

    private class CampaignMapper : RowMapper<Campaign> {
        override fun map(rs: ResultSet, ctx: StatementContext): Campaign = Campaign().apply {
            id = rs.getObject("id", UUID::class.java)
            title = rs.getString("title")
            val statusStr = rs.getString("status")
            if (statusStr != null) {
                status = CampaignStatus.valueOf(statusStr)
            }
            startedOn = rs.getTimestamp("started_on")?.toInstant()
            gm = GM().apply { id = rs.getObject("gm_id", UUID::class.java) }
            setting = Setting().apply { id = rs.getObject("setting_id", UUID::class.java) }
        }
    }
}
