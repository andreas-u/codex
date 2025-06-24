package org.fg.ttrpg.campaign

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.fg.ttrpg.account.GM
import org.fg.ttrpg.setting.SettingObject
import org.fg.ttrpg.setting.Template
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class CampaignObjectRepository @Inject constructor(private val jdbi: Jdbi) {

    fun listByCampaignAndGm(campaignId: UUID, gmId: UUID): List<CampaignObject> =
        jdbi.withHandle<List<CampaignObject>, Exception> { handle ->
            handle.createQuery("SELECT id, name, description, campaign_id, setting_object_id, gm_id, template_id, override_mode, payload, created_at FROM campaign_object WHERE campaign_id = :campaignId AND gm_id = :gmId")
                .bind("campaignId", campaignId)
                .bind("gmId", gmId)
                .map(CampaignObjectMapper())
                .list()
        }

    fun findById(id: UUID): CampaignObject? =
        jdbi.withHandle<CampaignObject?, Exception> { handle ->
            handle.createQuery("SELECT id, name, description, campaign_id, setting_object_id, gm_id, template_id, override_mode, payload, created_at FROM campaign_object WHERE id = :id")
                .bind("id", id)
                .map(CampaignObjectMapper())
                .findOne()
                .orElse(null)
        }

    fun findByIdForGm(id: UUID, gmId: UUID): CampaignObject? =
        jdbi.withHandle<CampaignObject?, Exception> { handle ->
            handle.createQuery("SELECT id, name, description, campaign_id, setting_object_id, gm_id, template_id, override_mode, payload, created_at FROM campaign_object WHERE id = :id AND gm_id = :gmId")
                .bind("id", id)
                .bind("gmId", gmId)
                .map(CampaignObjectMapper())
                .findOne()
                .orElse(null)
        }

    fun persist(obj: CampaignObject) {
        jdbi.useHandle<Exception> { handle ->
            handle.createUpdate("INSERT INTO campaign_object (id, name, description, campaign_id, setting_object_id, gm_id, template_id, override_mode, payload, created_at) VALUES (:id, :name, :description, :campaignId, :settingObjectId, :gmId, :templateId, :overrideMode, :payload::jsonb, :createdAt)")
                .bind("id", obj.id)
                .bind("name", obj.title)
                .bind("description", obj.description)
                .bind("campaignId", obj.campaign?.id)
                .bind("settingObjectId", obj.settingObject?.id)
                .bind("gmId", obj.gm?.id)
                .bind("templateId", obj.template?.id)
                .bind("overrideMode", obj.overrideMode)
                .bind("payload", obj.payload)
                .bind("createdAt", obj.createdAt)
                .execute()
        }
    }

    fun updatePayload(id: UUID, payload: String) {
        jdbi.useHandle<Exception> { handle ->
            handle.createUpdate("UPDATE campaign_object SET payload = :payload WHERE id = :id")
                .bind("id", id)
                .bind("payload", payload)
                .execute()
        }
    }

    fun update(obj: CampaignObject) {
        jdbi.useHandle<Exception> { handle ->
            handle.createUpdate("""
                UPDATE campaign_object SET
                    name = :name,
                    description = :description,
                    campaign_id = :campaignId,
                    setting_object_id = :settingObjectId,
                    gm_id = :gmId,
                    template_id = :templateId,
                    override_mode = :overrideMode,
                    payload = :payload::jsonb
                WHERE id = :id
            """)
                .bind("id", obj.id)
                .bind("name", obj.title)
                .bind("description", obj.description)
                .bind("campaignId", obj.campaign?.id)
                .bind("settingObjectId", obj.settingObject?.id)
                .bind("gmId", obj.gm?.id)
                .bind("templateId", obj.template?.id)
                .bind("overrideMode", obj.overrideMode)
                .bind("payload", obj.payload)
                .execute()
        }
    }

    private class CampaignObjectMapper : RowMapper<CampaignObject> {
        override fun map(rs: ResultSet, ctx: StatementContext): CampaignObject = CampaignObject().apply {
            id = rs.getObject("id", UUID::class.java)
            title = rs.getString("name")
            description = rs.getString("description")
            campaign = Campaign().apply { id = rs.getObject("campaign_id", UUID::class.java) }
            settingObject = SettingObject().apply { id = rs.getObject("setting_object_id", UUID::class.java) }
            gm = GM().apply { id = rs.getObject("gm_id", UUID::class.java) }
            template = Template().apply { id = rs.getObject("template_id", UUID::class.java) }
            overrideMode = rs.getString("override_mode")
            payload = rs.getString("payload")
            createdAt = rs.getTimestamp("created_at").toInstant()
        }
    }
}
