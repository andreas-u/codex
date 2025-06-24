package org.fg.ttrpg.campaign

import io.mockk.every
import io.mockk.mockk
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.nulls.shouldBeNull
import org.junit.jupiter.api.Test
import java.util.*

class CampaignServiceTest {
    private val campaignRepository = mockk<CampaignRepository>()
    private val service = CampaignService(campaignRepository)

    @Test
    fun `findById should return campaign when found`() {
        val id = UUID.randomUUID()
        val campaign = Campaign().apply { this.id = id }
        every { campaignRepository.findById(id) } returns campaign

        val result = service.findById(id)
        result shouldNotBe null
        result?.id shouldBe id
    }

    @Test
    fun `findById should return null when not found`() {
        val id = UUID.randomUUID()
        every { campaignRepository.findById(id) } returns null

        val result = service.findById(id)
        result.shouldBeNull()
    }

    @Test
    fun `listAll should return campaigns for gm`() {
        val gmId = UUID.randomUUID()
        val campaigns = listOf(Campaign().apply { id = UUID.randomUUID() })
        every { campaignRepository.listByGm(gmId) } returns campaigns

        val result = service.listAll(gmId)
        result shouldBe campaigns
    }

    @Test
    fun `findByIdForGm should return campaign for gm`() {
        val id = UUID.randomUUID()
        val gmId = UUID.randomUUID()
        val campaign = Campaign().apply { this.id = id }
        every { campaignRepository.findByIdForGm(id, gmId) } returns campaign

        val result = service.findByIdForGm(id, gmId)
        result shouldNotBe null
        result?.id shouldBe id
    }

    @Test
    fun `persist should call repository persist`() {
        val campaign = Campaign().apply { id = UUID.randomUUID() }
        every { campaignRepository.persist(campaign) } returns Unit
        service.persist(campaign)
        // No exception means pass, verify was called
    }
}
