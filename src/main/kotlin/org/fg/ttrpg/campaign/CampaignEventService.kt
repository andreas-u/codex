package org.fg.ttrpg.campaign

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.fg.ttrpg.infra.merge.MergeService
import org.fg.ttrpg.timeline.TimelineEvent
import java.util.UUID

@ApplicationScoped
class CampaignEventService @Inject constructor(
    private val repository: CampaignEventOverrideRepository,
    private val merge: MergeService
) {
    private val mapper = ObjectMapper()

    fun list(): List<CampaignEventOverride> = repository.list()

    fun findById(id: UUID): CampaignEventOverride? = repository.findById(id)

    fun listByCampaign(campaignId: UUID): List<CampaignEventOverride> =
        repository.listByCampaign(campaignId)

    fun findByCampaignAndEvent(campaignId: UUID, eventId: UUID): CampaignEventOverride? =
        repository.findByCampaignAndEvent(campaignId, eventId)

    fun persist(override: CampaignEventOverride) {
        repository.persist(override)
    }

    fun update(override: CampaignEventOverride) {
        repository.update(override)
    }

    fun deleteById(id: UUID) {
        repository.deleteById(id)
    }

    /**
     * Apply an override to a base timeline event and return the result.
     * DELETE overrides return null.
     */
    fun applyOverride(base: TimelineEvent, override: CampaignEventOverride): TimelineEvent? {
        return when (override.overrideMode) {
            OverrideMode.DELETE -> null
            OverrideMode.REPLACE -> mapper.readValue(override.payload ?: "{}", TimelineEvent::class.java)
                .apply {
                    id = base.id
                    calendar = base.calendar
                }
            OverrideMode.PATCH -> {
                val baseJson = mapper.writeValueAsString(base)
                val merged = merge.merge(baseJson, override.payload ?: "{}")
                mapper.readValue(merged, TimelineEvent::class.java).apply {
                    id = base.id
                    calendar = base.calendar
                }
            }
            null -> base
        }
    }
}
