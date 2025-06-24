package org.fg.ttrpg.campaign

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.fg.ttrpg.infra.merge.MergeService
import org.fg.ttrpg.timeline.TimelineEvent
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.nulls.shouldBeNull
import org.junit.jupiter.api.Test
import java.util.*

class CampaignEventServiceTest {
    private val repository = mockk<CampaignEventOverrideRepository>()
    private val merge = mockk<MergeService>()
    private val service = CampaignEventService(repository, merge)
    private val mapper = ObjectMapper()

    @Test
    fun `list should return all overrides`() {
        val overrides = listOf(CampaignEventOverride())
        every { repository.list() } returns overrides
        service.list() shouldBe overrides
    }

    @Test
    fun `findById should return override`() {
        val id = UUID.randomUUID()
        val override = CampaignEventOverride().apply { this.id = id }
        every { repository.findById(id) } returns override
        service.findById(id) shouldBe override
    }

    @Test
    fun `listByCampaign should return overrides`() {
        val campaignId = UUID.randomUUID()
        val overrides = listOf(CampaignEventOverride())
        every { repository.listByCampaign(campaignId) } returns overrides
        service.listByCampaign(campaignId) shouldBe overrides
    }

    @Test
    fun `findByCampaignAndEvent should return override`() {
        val campaignId = UUID.randomUUID()
        val eventId = UUID.randomUUID()
        val override = CampaignEventOverride()
        every { repository.findByCampaignAndEvent(campaignId, eventId) } returns override
        service.findByCampaignAndEvent(campaignId, eventId) shouldBe override
    }

    @Test
    fun `persist should call repository persist`() {
        val override = CampaignEventOverride()
        every { repository.persist(override) } returns 1
        service.persist(override) shouldBe 1
    }

    @Test
    fun `update should call repository update`() {
        val override = CampaignEventOverride()
        every { repository.update(override) } returns Unit
        service.update(override)
    }

    @Test
    fun `deleteById should call repository deleteById`() {
        val id = UUID.randomUUID()
        every { repository.deleteById(id) } returns Unit
        service.deleteById(id)
    }

    @Test
    fun `applyOverride should return null for DELETE`() {
        val base = TimelineEvent().apply { id = UUID.randomUUID() }
        val override = CampaignEventOverride().apply { overrideMode = OverrideMode.DELETE }
        service.applyOverride(base, override).shouldBeNull()
    }

    @Test
    fun `applyOverride should return replaced event for REPLACE`() {
        val base = TimelineEvent().apply { id = UUID.randomUUID() }
        val replaced = TimelineEvent().apply { id = base.id }
        val payload = mapper.writeValueAsString(replaced)
        val override = CampaignEventOverride().apply {
            overrideMode = OverrideMode.REPLACE
            this.payload = payload
        }
        val result = service.applyOverride(base, override)
        result shouldNotBe null
        result?.id shouldBe base.id
    }

    @Test
    fun `applyOverride should patch event for PATCH`() {
        val base = TimelineEvent().apply { id = UUID.randomUUID(); title = "old" }
        val override = CampaignEventOverride().apply {
            overrideMode = OverrideMode.PATCH
            payload = "{\"title\":\"new\"}"
        }
        every { merge.merge(any<String>(), any<String>()) } returns "{\"id\":\"${base.id}\",\"title\":\"new\"}"
        val result = service.applyOverride(base, override)
        result shouldNotBe null
        result?.title shouldBe "new"
    }

    @Test
    fun `applyOverride should return base for null overrideMode`() {
        val base = TimelineEvent().apply { id = UUID.randomUUID() }
        val override = CampaignEventOverride().apply { overrideMode = null }
        val result = service.applyOverride(base, override)
        result shouldBe base
    }
}
