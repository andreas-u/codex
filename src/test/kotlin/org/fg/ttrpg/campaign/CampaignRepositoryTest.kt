package org.fg.ttrpg.campaign

import io.quarkus.test.junit.QuarkusTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldContain
import jakarta.inject.Inject
import org.fg.ttrpg.account.GM
import org.fg.ttrpg.setting.Setting
import org.fg.ttrpg.testutils.IntegrationTestHelper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

@QuarkusTest
class CampaignRepositoryTest : IntegrationTestHelper() {




    @BeforeEach
    fun setup() {
    }

    @Test
    fun `findById should return campaign`() {
        val gm = createGm(UUID.randomUUID())
        val setting = createSetting(UUID.randomUUID(), gm.id!!)
        val campaign = createCampaign(UUID.randomUUID(), gm.id!!, setting)
        val found = campaignRepo.findById(campaign.id!!)
        found shouldNotBe null
        found?.id shouldBe campaign.id
    }

    @Test
    fun `findByIdForGm should return campaign for correct gm`() {
        val gm = createGm(UUID.randomUUID())
        val setting = createSetting(UUID.randomUUID(), gm.id!!)
        val campaign = createCampaign(UUID.randomUUID(), gm.id!!, setting)
        val found = campaignRepo.findByIdForGm(campaign.id!!, gm.id!!)
        found shouldNotBe null
        found?.id shouldBe campaign.id
    }

    @Test
    fun `listByGm should return campaigns for gm`() {
        val gm = createGm(UUID.randomUUID())
        val setting = createSetting(UUID.randomUUID(), gm.id!!)
        val campaign = createCampaign(UUID.randomUUID(), gm.id!!, setting)
        val list = campaignRepo.listByGm(gm.id!!)
        list.map { it.id } shouldContain campaign.id
    }
}
