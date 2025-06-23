package org.fg.ttrpg.campaign

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import java.util.UUID

@ApplicationScoped
class CampaignObjectRepository @Inject constructor(private val dsl: DSLContext) {
    private val CAMPAIGN_OBJECT = DSL.table("campaign_object")
    private val CO_ID = DSL.field("id", java.util.UUID::class.java)
    private val CO_NAME = DSL.field("name", String::class.java)
    private val CO_DESCRIPTION = DSL.field("description", String::class.java)
    private val CO_CAMPAIGN_ID = DSL.field("campaign_id", java.util.UUID::class.java)
    private val CO_SETTING_OBJECT_ID = DSL.field("setting_object_id", java.util.UUID::class.java)
    private val CO_GM_ID = DSL.field("gm_id", java.util.UUID::class.java)
    private val CO_TEMPLATE_ID = DSL.field("template_id", java.util.UUID::class.java)
    private val CO_OVERRIDE_MODE = DSL.field("override_mode", String::class.java)
    private val CO_PAYLOAD = DSL.field("payload", String::class.java)
    private val CO_CREATED_AT = DSL.field("created_at", java.time.Instant::class.java)

    fun listByCampaignAndGm(campaignId: UUID, gmId: UUID): List<CampaignObject> =
        dsl.selectFrom(CAMPAIGN_OBJECT)
            .where(
                CO_CAMPAIGN_ID.eq(campaignId)
                    .and(CO_GM_ID.eq(gmId))
            )
            .fetch(::toObject)

    fun findById(id: UUID): CampaignObject? =
        dsl.selectFrom(CAMPAIGN_OBJECT)
            .where(CO_ID.eq(id))
            .fetchOne(::toObject)

    fun findByIdForGm(id: UUID, gmId: UUID): CampaignObject? =
        dsl.selectFrom(CAMPAIGN_OBJECT)
            .where(CO_ID.eq(id).and(CO_GM_ID.eq(gmId)))
            .fetchOne(::toObject)

    fun persist(obj: CampaignObject) {
        dsl.insertInto(CAMPAIGN_OBJECT)
            .set(CO_ID, obj.id)
            .set(CO_NAME, obj.title)
            .set(CO_DESCRIPTION, obj.description)
            .set(CO_CAMPAIGN_ID, obj.campaign?.id)
            .set(CO_SETTING_OBJECT_ID, obj.settingObject?.id)
            .set(CO_GM_ID, obj.gm?.id)
            .set(CO_TEMPLATE_ID, obj.template?.id)
            .set(CO_OVERRIDE_MODE, obj.overrideMode)
            .set(CO_PAYLOAD, obj.payload)
            .set(CO_CREATED_AT, obj.createdAt)
            .execute()
    }

    private fun toObject(record: Record): CampaignObject = CampaignObject().apply {
        id = record.get(CO_ID)
        title = record.get(CO_NAME)
        description = record.get(CO_DESCRIPTION)
        campaign = Campaign().apply { id = record.get(CO_CAMPAIGN_ID) }
        settingObject = org.fg.ttrpg.setting.SettingObject().apply { id = record.get(CO_SETTING_OBJECT_ID) }
        gm = org.fg.ttrpg.account.GM().apply { id = record.get(CO_GM_ID) }
        template = org.fg.ttrpg.setting.Template().apply { id = record.get(CO_TEMPLATE_ID) }
        overrideMode = record.get(CO_OVERRIDE_MODE)
        payload = record.get(CO_PAYLOAD)
        createdAt = record.get(CO_CREATED_AT)
    }
}
