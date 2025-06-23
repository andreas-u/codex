package org.fg.ttrpg.campaign

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.generated.tables.references.CAMPAIGN_OBJECT
import java.util.UUID

@ApplicationScoped
class CampaignObjectRepository @Inject constructor(private val dsl: DSLContext) {

    fun listByCampaignAndGm(campaignId: UUID, gmId: UUID): List<CampaignObject> =
        dsl.selectFrom(CAMPAIGN_OBJECT)
            .where(
                CAMPAIGN_OBJECT.CAMPAIGN_ID.eq(campaignId)
                    .and(CAMPAIGN_OBJECT.GM_ID.eq(gmId))
            )
            .fetch(::toObject)

    fun findById(id: UUID): CampaignObject? =
        dsl.selectFrom(CAMPAIGN_OBJECT)
            .where(CAMPAIGN_OBJECT.ID.eq(id))
            .fetchOne(::toObject)

    fun findByIdForGm(id: UUID, gmId: UUID): CampaignObject? =
        dsl.selectFrom(CAMPAIGN_OBJECT)
            .where(CAMPAIGN_OBJECT.ID.eq(id).and(CAMPAIGN_OBJECT.GM_ID.eq(gmId)))
            .fetchOne(::toObject)

    fun persist(obj: CampaignObject) {
        dsl.insertInto(CAMPAIGN_OBJECT)
            .set(CAMPAIGN_OBJECT.ID, obj.id)
            .set(CAMPAIGN_OBJECT.NAME, obj.title)
            .set(CAMPAIGN_OBJECT.DESCRIPTION, obj.description)
            .set(CAMPAIGN_OBJECT.CAMPAIGN_ID, obj.campaign?.id)
            .set(CAMPAIGN_OBJECT.SETTING_OBJECT_ID, obj.settingObject?.id)
            .set(CAMPAIGN_OBJECT.GM_ID, obj.gm?.id)
            .set(CAMPAIGN_OBJECT.TEMPLATE_ID, obj.template?.id)
            .set(CAMPAIGN_OBJECT.OVERRIDE_MODE, obj.overrideMode)
            .set(CAMPAIGN_OBJECT.PAYLOAD, obj.payload)
            .set(CAMPAIGN_OBJECT.CREATED_AT, obj.createdAt)
            .execute()
    }

    private fun toObject(record: Record): CampaignObject = CampaignObject().apply {
        id = record.get(CAMPAIGN_OBJECT.ID)
        title = record.get(CAMPAIGN_OBJECT.NAME)
        description = record.get(CAMPAIGN_OBJECT.DESCRIPTION)
        campaign = Campaign().apply { id = record.get(CAMPAIGN_OBJECT.CAMPAIGN_ID) }
        settingObject = org.fg.ttrpg.setting.SettingObject().apply { id = record.get(CAMPAIGN_OBJECT.SETTING_OBJECT_ID) }
        gm = org.fg.ttrpg.account.GM().apply { id = record.get(CAMPAIGN_OBJECT.GM_ID) }
        template = org.fg.ttrpg.setting.Template().apply { id = record.get(CAMPAIGN_OBJECT.TEMPLATE_ID) }
        overrideMode = record.get(CAMPAIGN_OBJECT.OVERRIDE_MODE)
        payload = record.get(CAMPAIGN_OBJECT.PAYLOAD)
        createdAt = record.get(CAMPAIGN_OBJECT.CREATED_AT)
    }
}
