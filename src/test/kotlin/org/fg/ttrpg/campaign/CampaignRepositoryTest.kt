package org.fg.ttrpg.campaign

import io.kotest.matchers.shouldBe
import io.quarkus.test.TestTransaction
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.fg.ttrpg.account.GM
import org.fg.ttrpg.account.GMRepository
import org.fg.ttrpg.setting.Setting
import org.fg.ttrpg.setting.SettingRepository
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

@QuarkusTest
class CampaignRepositoryTest {
    @Inject
    lateinit var gmRepo: GMRepository

    @Inject
    lateinit var settingRepo: SettingRepository

    @Inject
    lateinit var campaignRepo: CampaignRepository

    @Test
    @TestTransaction
    fun `persist and fetch campaign`() {
        val gm = GM().apply {
            id = UUID.randomUUID()
            username = "gm"
            email = "gm@example.com"
        }
        gmRepo.persist(gm)

        val setting = Setting().apply {
            id = UUID.randomUUID()
            title = "world"
            this.gm = gm
        }
        settingRepo.persist(setting)

        val campaign = Campaign().apply {
            id = UUID.randomUUID()
            title = "camp"
            startedOn = Instant.now()
            this.gm = gm
            this.setting = setting
        }
        campaignRepo.persist(campaign)

        val byId = campaignRepo.findById(campaign.id!!)
        byId!!.gm?.id shouldBe gm.id

        val byIdForGm = campaignRepo.findByIdForGm(campaign.id!!, gm.id!!)
        byIdForGm!!.id shouldBe campaign.id

        val list = campaignRepo.listByGm(gm.id!!)
        list.size shouldBe 1
        list.first().id shouldBe campaign.id
    }
}
