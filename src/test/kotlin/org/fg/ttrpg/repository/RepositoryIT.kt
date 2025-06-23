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
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.UUID

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
            id = UUID.randomUUID()
            username = "gm1"
            email = "gm@example.com"
        }
        gmRepo.persist(gm)

        val genre = Genre().apply {
            id = UUID.randomUUID()
            name = "fantasy"
        }
        genreRepo.persist(genre)

        val setting = Setting().apply {
            id = UUID.randomUUID()
            name = "world"
            genres.add(genre)
        }
        settingRepo.persist(setting)

        val template = Template().apply {
            id = UUID.randomUUID()
            name = "template"
            this.setting = setting
        }
        templateRepo.persist(template)

        val settingObject = SettingObject().apply {
            id = UUID.randomUUID()
            name = "object"
            this.setting = setting
        }
        settingObjectRepo.persist(settingObject)

        val campaign = Campaign().apply {
            id = UUID.randomUUID()
            name = "camp"
            this.gm = gm
            this.setting = setting
        }
        campaignRepo.persist(campaign)

        val campaignObject = CampaignObject().apply {
            id = UUID.randomUUID()
            name = "campobj"
            this.campaign = campaign
            this.settingObject = settingObject
        }
        campaignObjectRepo.persist(campaignObject)

        gmRepo.count() shouldBe 1
        genreRepo.count() shouldBe 1
        settingRepo.count() shouldBe 1
        templateRepo.count() shouldBe 1
        templateRepo.listByGenre("fantasy").size shouldBe 1
        templateRepo.listByType("template").size shouldBe 1
        settingObjectRepo.count() shouldBe 1
        campaignRepo.count() shouldBe 1
        campaignObjectRepo.count() shouldBe 1
    }
}
