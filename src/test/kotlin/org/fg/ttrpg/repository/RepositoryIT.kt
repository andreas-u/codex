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
        val gm1 = GM().apply {
            id = UUID.randomUUID()
            username = "gm1"
            email = "gm1@example.com"
        }
        val gm2 = GM().apply {
            id = UUID.randomUUID()
            username = "gm2"
            email = "gm2@example.com"
        }
        gmRepo.persist(gm1)
        gmRepo.persist(gm2)

        val genre = Genre().apply {
            id = UUID.randomUUID()
            name = "fantasy"
        }
        genreRepo.persist(genre)

        val setting1 = Setting().apply {
            id = UUID.randomUUID()
            name = "world1"
            gm = gm1
            genres.add(genre)
        }
        val setting2 = Setting().apply {
            id = UUID.randomUUID()
            name = "world2"
            gm = gm2
            genres.add(genre)
        }
        settingRepo.persist(setting1)
        settingRepo.persist(setting2)

        val template1 = Template().apply {
            id = UUID.randomUUID()
            name = "template1"
            this.setting = setting1
            this.gm = gm1
        }
        val template2 = Template().apply {
            id = UUID.randomUUID()
            name = "template2"
            this.setting = setting2
            this.gm = gm2
        }
        templateRepo.persist(template1)
        templateRepo.persist(template2)

        val settingObject1 = SettingObject().apply {
            id = UUID.randomUUID()
            name = "object1"
            this.setting = setting1
            this.gm = gm1
        }
        val settingObject2 = SettingObject().apply {
            id = UUID.randomUUID()
            name = "object2"
            this.setting = setting2
            this.gm = gm2
        }
        settingObjectRepo.persist(settingObject1)
        settingObjectRepo.persist(settingObject2)

        val campaign1 = Campaign().apply {
            id = UUID.randomUUID()
            name = "camp1"
            this.gm = gm1
            this.setting = setting1
        }
        val campaign2 = Campaign().apply {
            id = UUID.randomUUID()
            name = "camp2"
            this.gm = gm2
            this.setting = setting2
        }
        campaignRepo.persist(campaign1)
        campaignRepo.persist(campaign2)

        val campaignObject1 = CampaignObject().apply {
            id = UUID.randomUUID()
            name = "campobj1"
            this.campaign = campaign1
            this.settingObject = settingObject1
            this.gm = gm1
        }
        val campaignObject2 = CampaignObject().apply {
            id = UUID.randomUUID()
            name = "campobj2"
            this.campaign = campaign2
            this.settingObject = settingObject2
            this.gm = gm2
        }
        campaignObjectRepo.persist(campaignObject1)
        campaignObjectRepo.persist(campaignObject2)

        gmRepo.count() shouldBe 2
        genreRepo.count() shouldBe 1
        settingRepo.listByGm(gm1.id!!).size shouldBe 1
        settingRepo.listByGm(gm2.id!!).size shouldBe 1
        templateRepo.listByGm(gm1.id!!).size shouldBe 1
        templateRepo.listByGm(gm2.id!!).size shouldBe 1
        settingObjectRepo.listBySettingAndGm(setting1.id!!, gm1.id!!).size shouldBe 1
        settingObjectRepo.listBySettingAndGm(setting2.id!!, gm2.id!!).size shouldBe 1
        campaignRepo.listByGm(gm1.id!!).size shouldBe 1
        campaignRepo.listByGm(gm2.id!!).size shouldBe 1
        campaignObjectRepo.findByIdForGm(campaignObject1.id!!, gm1.id!!)!!.id shouldBe campaignObject1.id
    }
}
