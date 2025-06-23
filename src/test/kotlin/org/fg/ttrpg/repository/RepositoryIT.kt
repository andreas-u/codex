package org.fg.ttrpg.repository

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.TestTransaction
import jakarta.inject.Inject
import org.fg.ttrpg.account.GM
import org.fg.ttrpg.account.GMRepository
import org.fg.ttrpg.campaign.Campaign
import org.fg.ttrpg.campaign.CampaignObject
import org.fg.ttrpg.campaign.CampaignObjectRepository
import org.fg.ttrpg.campaign.CampaignRepository
import org.fg.ttrpg.genre.Genre
import org.fg.ttrpg.genre.GenreRepository
import org.fg.ttrpg.setting.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@QuarkusTest
class RepositoryIT {
    @Inject
    lateinit var gmRepo: GMRepository
    @Inject
    lateinit var genreRepo: GenreRepository
    @Inject
    lateinit var settingRepo: SettingRepository
    @Inject
    lateinit var templateRepo: TemplateRepository
    @Inject
    lateinit var settingObjectRepo: SettingObjectRepository
    @Inject
    lateinit var campaignRepo: CampaignRepository
    @Inject
    lateinit var campaignObjectRepo: CampaignObjectRepository

    @Test
    @TestTransaction
    fun `persist and fetch entities`() {
        val gm = GM().apply {
            username = "gm1"
            email = "gm@example.com"
        }
        gmRepo.persist(gm)

        val genre = Genre().apply { name = "fantasy" }
        genreRepo.persist(genre)

        val setting = Setting().apply {
            name = "world"
            genres.add(genre)
        }
        settingRepo.persist(setting)

        val template = Template().apply {
            name = "template"
            this.setting = setting
        }
        templateRepo.persist(template)

        val settingObject = SettingObject().apply {
            name = "object"
            this.setting = setting
        }
        settingObjectRepo.persist(settingObject)

        val campaign = Campaign().apply {
            name = "camp"
            this.gm = gm
            this.setting = setting
        }
        campaignRepo.persist(campaign)

        val campaignObject = CampaignObject().apply {
            name = "campobj"
            this.campaign = campaign
            this.settingObject = settingObject
        }
        campaignObjectRepo.persist(campaignObject)

        assertEquals(1, gmRepo.count())
        assertEquals(1, genreRepo.count())
        assertEquals(1, settingRepo.count())
        assertEquals(1, templateRepo.count())
        assertEquals(1, settingObjectRepo.count())
        assertEquals(1, campaignRepo.count())
        assertEquals(1, campaignObjectRepo.count())
    }
}
