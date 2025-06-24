package org.fg.ttrpg.campaign

import io.quarkus.test.junit.QuarkusTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldContain
import jakarta.inject.Inject
import org.fg.ttrpg.account.GM
import org.fg.ttrpg.setting.Setting
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

@QuarkusTest
class CampaignRepositoryTest {
    @Inject
    lateinit var repository: CampaignRepository

    private lateinit var gm: GM
    private lateinit var setting: Setting
    private lateinit var campaign: Campaign

    @BeforeEach
    fun setup() {
        gm = GM().apply { id = UUID.randomUUID(); username = "gm" }
        setting = Setting().apply { id = UUID.randomUUID(); title = "setting"; this.gm = gm }
        campaign = Campaign().apply {
            id = UUID.randomUUID()
            title = "Test Campaign"
            startedOn = Instant.now()
            this.gm = gm
            this.setting = setting
        }
        repository.persist(campaign)
    }

    @Test
    fun `findById should return campaign`() {
        val found = repository.findById(campaign.id!!)
        found shouldNotBe null
        found?.id shouldBe campaign.id
    }

    @Test
    fun `findByIdForGm should return campaign for correct gm`() {
        val found = repository.findByIdForGm(campaign.id!!, gm.id!!)
        found shouldNotBe null
        found?.id shouldBe campaign.id
    }

    @Test
    fun `listByGm should return campaigns for gm`() {
        val list = repository.listByGm(gm.id!!)
        list.map { it.id } shouldContain campaign.id
    }
}
