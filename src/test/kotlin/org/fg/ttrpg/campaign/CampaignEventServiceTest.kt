package org.fg.ttrpg.campaign

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.fg.ttrpg.infra.merge.MergeService
import org.fg.ttrpg.timeline.TimelineEvent
import org.junit.jupiter.api.Assertions.*
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
        assertEquals(overrides, service.list())
    }

    @Test
    fun `findById should return override`() {
        val id = UUID.randomUUID()
        val override = CampaignEventOverride().apply { this.id = id }
        every { repository.findById(id) } returns override
        assertEquals(override, service.findById(id))
    }

    @Test
    fun `listByCampaign should return overrides`() {
        val campaignId = UUID.randomUUID()
        val overrides = listOf(CampaignEventOverride())
        every { repository.listByCampaign(campaignId) } returns overrides
        assertEquals(overrides, service.listByCampaign(campaignId))
    }

    @Test
    fun `findByCampaignAndEvent should return override`() {
        val campaignId = UUID.randomUUID()
        val eventId = UUID.randomUUID()
        val override = CampaignEventOverride()
        every { repository.findByCampaignAndEvent(campaignId, eventId) } returns override
        assertEquals(override, service.findByCampaignAndEvent(campaignId, eventId))
    }

    @Test
    fun `persist should call repository persist`() {
        val override = CampaignEventOverride()
        every { repository.persist(override) } returns 1
        assertEquals(1, service.persist(override))
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
        assertNull(service.applyOverride(base, override))
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
        assertNotNull(result)
        assertEquals(base.id, result?.id)
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
        assertNotNull(result)
        assertEquals("new", result?.title)
    }

    @Test
    fun `applyOverride should return base for null overrideMode`() {
        val base = TimelineEvent().apply { id = UUID.randomUUID() }
        val override = CampaignEventOverride().apply { overrideMode = null }
        val result = service.applyOverride(base, override)
        assertEquals(base, result)
    }
}
