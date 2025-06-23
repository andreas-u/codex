package org.fg.ttrpg.campaign

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.generated.tables.references.CAMPAIGN
import java.util.UUID

@ApplicationScoped
class CampaignRepository @Inject constructor(private val dsl: DSLContext) {

    fun listByGm(gmId: UUID): List<Campaign> =
        dsl.selectFrom(CAMPAIGN)
            .where(CAMPAIGN.GM_ID.eq(gmId))
            .fetch(::toCampaign)

    fun findById(id: UUID): Campaign? =
        dsl.selectFrom(CAMPAIGN)
            .where(CAMPAIGN.ID.eq(id))
            .fetchOne(::toCampaign)

    fun findByIdForGm(id: UUID, gmId: UUID): Campaign? =
        dsl.selectFrom(CAMPAIGN)
            .where(CAMPAIGN.ID.eq(id).and(CAMPAIGN.GM_ID.eq(gmId)))
            .fetchOne(::toCampaign)

    fun persist(campaign: Campaign) {
        dsl.insertInto(CAMPAIGN)
            .set(CAMPAIGN.ID, campaign.id)
            .set(CAMPAIGN.NAME, campaign.title)
            .set(CAMPAIGN.STARTED_ON, campaign.startedOn)
            .set(CAMPAIGN.GM_ID, campaign.gm?.id)
            .set(CAMPAIGN.SETTING_ID, campaign.setting?.id)
            .execute()
    }

    private fun toCampaign(record: Record): Campaign = Campaign().apply {
        id = record.get(CAMPAIGN.ID)
        title = record.get(CAMPAIGN.NAME)
        startedOn = record.get(CAMPAIGN.STARTED_ON)
        gm = org.fg.ttrpg.account.GM().apply { id = record.get(CAMPAIGN.GM_ID) }
        setting = org.fg.ttrpg.setting.Setting().apply { id = record.get(CAMPAIGN.SETTING_ID) }
    }
}
