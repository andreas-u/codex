package org.fg.ttrpg.campaign.resource

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.jwt.JsonWebToken
import org.fg.ttrpg.calendar.CalendarService
import org.fg.ttrpg.campaign.*
import org.fg.ttrpg.common.dto.CampaignEventOverrideDTO
import org.fg.ttrpg.common.dto.TimelineEventDTO
import org.fg.ttrpg.setting.SettingService
import org.fg.ttrpg.timeline.TimelineEvent
import org.fg.ttrpg.timeline.TimelineService
import java.time.Instant
import java.util.UUID

@Path("/api/campaigns")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class CampaignEventResource @Inject constructor(
    private val campaignService: CampaignService,
    private val eventService: CampaignEventService,
    private val calendarService: CalendarService,
    private val timelineService: TimelineService,
    private val settingService: SettingService,
    private val jwt: JsonWebToken,
) {
    private val mapper = ObjectMapper()
    private fun gmId() = UUID.fromString(jwt.getClaim("gmId"))

    @PATCH
    @Path("{cid}/events/{eid}")
    @Transactional
    fun patchEvent(
        @PathParam("cid") cid: UUID,
        @PathParam("eid") eid: UUID,
        dto: CampaignEventOverrideDTO,
    ): TimelineEventDTO {
        val campaign = campaignService.findByIdForGm(cid, gmId()) ?: throw NotFoundException()
        val base = timelineService.findById(eid) ?: throw NotFoundException()
        var override = eventService.findByCampaignAndEvent(cid, eid)
        if (override == null) {
            override = CampaignEventOverride().apply {
                id = UUID.randomUUID()
                this.campaign = campaign
                this.baseEvent = base
                createdAt = Instant.now()
            }
            eventService.persist(override)
        }
        override.overrideMode = OverrideMode.valueOf(dto.overrideMode)
        override.payload = dto.payload
        eventService.update(override)
        val result = eventService.applyOverride(base, override) ?: throw NotFoundException()
        return result.toDto()
    }

    @GET
    @Path("{cid}/timeline")
    fun timeline(
        @PathParam("cid") cid: UUID,
        @QueryParam("from") from: Int?,
        @QueryParam("to") to: Int?,
    ): List<TimelineEventDTO> {
        val campaign = campaignService.findByIdForGm(cid, gmId()) ?: throw NotFoundException()
        val settingId = campaign.setting?.id ?: throw NotFoundException()
        val calendars = calendarService.listBySetting(settingId)
        val baseEvents = calendars.flatMap { timelineService.listByCalendar(it.id!!) }.toMutableList()
        val overrides = eventService.listByCampaign(cid)
        overrides.forEach { ov ->
            val baseId = ov.baseEvent?.id
            if (baseId != null) {
                val base = baseEvents.find { it.id == baseId }
                if (base != null) {
                    baseEvents.remove(base)
                    eventService.applyOverride(base, ov)?.let { baseEvents.add(it) }
                }
            } else {
                val newEvent = mapper.readValue(ov.payload ?: "{}", TimelineEvent::class.java)
                baseEvents.add(newEvent)
            }
        }
        val start = from ?: Int.MIN_VALUE
        val end = to ?: Int.MAX_VALUE
        return baseEvents.filter { (it.startDay ?: 0) in start..end }.map { it.toDto() }
    }
}

private fun TimelineEvent.toDto() = TimelineEventDTO(
    id,
    calendar?.id ?: error("Calendar is null"),
    title ?: "",
    description,
    startDay ?: 0,
    endDay,
    objectRefs.map { it.id ?: error("id") },
    tags.toList(),
)
