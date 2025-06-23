package org.fg.ttrpg.campaign

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import java.util.UUID

@ApplicationScoped
class CampaignRepository @Inject constructor(private val dsl: DSLContext) {
    private val CAMPAIGN = DSL.table("campaign")
    private val CAMPAIGN_ID = DSL.field("id", java.util.UUID::class.java)
    private val CAMPAIGN_NAME = DSL.field("name", String::class.java)
    private val CAMPAIGN_STARTED_ON = DSL.field("started_on", java.time.Instant::class.java)
    private val CAMPAIGN_GM_ID = DSL.field("gm_id", java.util.UUID::class.java)
    private val CAMPAIGN_SETTING_ID = DSL.field("setting_id", java.util.UUID::class.java)

    fun listByGm(gmId: UUID): List<Campaign> =
        dsl.selectFrom(CAMPAIGN)
            .where(CAMPAIGN_GM_ID.eq(gmId))
            .fetch(::toCampaign)

    fun findById(id: UUID): Campaign? =
        dsl.selectFrom(CAMPAIGN)
            .where(CAMPAIGN_ID.eq(id))
            .fetchOne(::toCampaign)

    fun findByIdForGm(id: UUID, gmId: UUID): Campaign? =
        dsl.selectFrom(CAMPAIGN)
            .where(CAMPAIGN_ID.eq(id).and(CAMPAIGN_GM_ID.eq(gmId)))
            .fetchOne(::toCampaign)

    fun persist(campaign: Campaign) {
        dsl.insertInto(CAMPAIGN)
            .set(CAMPAIGN_ID, campaign.id)
            .set(CAMPAIGN_NAME, campaign.title)
            .set(CAMPAIGN_STARTED_ON, campaign.startedOn)
            .set(CAMPAIGN_GM_ID, campaign.gm?.id)
            .set(CAMPAIGN_SETTING_ID, campaign.setting?.id)
            .execute()
    }

    private fun toCampaign(record: Record): Campaign = Campaign().apply {
        id = record.get(CAMPAIGN_ID)
        title = record.get(CAMPAIGN_NAME)
        startedOn = record.get(CAMPAIGN_STARTED_ON)
        gm = org.fg.ttrpg.account.GM().apply { id = record.get(CAMPAIGN_GM_ID) }
        setting = org.fg.ttrpg.setting.Setting().apply { id = record.get(CAMPAIGN_SETTING_ID) }
    }
}
