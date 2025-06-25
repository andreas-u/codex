package org.fg.ttrpg.calendar.resource

import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.jwt.JsonWebToken
import org.fg.ttrpg.calendar.CalendarService
import org.fg.ttrpg.calendar.CalendarSystem
import org.fg.ttrpg.common.dto.CalendarDTO
import org.fg.ttrpg.common.dto.TimelineEventDTO
import org.fg.ttrpg.setting.SettingObject
import org.fg.ttrpg.setting.SettingService
import org.fg.ttrpg.timeline.TimelineEvent
import org.fg.ttrpg.timeline.TimelineService
import org.fg.ttrpg.auth.UserRepository
import java.util.*

@Path("/api/calendars")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class CalendarResource @Inject constructor(
    private val calendarService: CalendarService,
    private val settingService: SettingService,
    private val timelineService: TimelineService,
    private val jwt: JsonWebToken,
    private val userRepo: UserRepository,
) {
    private fun gmId(): UUID =
        userRepo.findById(UUID.fromString(jwt.getClaim("userId")))?.gm?.id ?: throw NotFoundException()


    @GET
    @Path("{id}")
    fun getCalendar(@PathParam("id") id: UUID): CalendarDTO {
        val calendar = calendarService.findById(id) ?: throw NotFoundException()
        settingService.findByIdForGm(calendar.setting?.id ?: error("Setting is null"), gmId())
            ?: throw NotFoundException()
        return calendar.toDto()
    }

    @GET
    @Path("{id}/events/{eventId}")
    fun getCalendar(@PathParam("id") id: UUID, @PathParam("eventId") eventId: UUID): TimelineEventDTO {
        val event = timelineService.findByIdForCalendar(eventId,id) ?: throw NotFoundException()
        return event.toDto()
    }

    @POST
    @Path("{id}/events")
    @Transactional
    fun createEvent(@PathParam("id") calendarId: UUID, dto: TimelineEventDTO): TimelineEventDTO {
        val calendar = calendarService.findById(calendarId) ?: throw NotFoundException()
        settingService.findByIdForGm(calendar.setting?.id ?: error("Setting is null"), gmId())
            ?: throw NotFoundException()
        val event = TimelineEvent().apply {
            title = dto.title
            description = dto.description
            startDay = dto.startDay
            endDay = dto.endDay
            objectRefs = dto.objectRefs.map { SettingObject().apply { id = it } }.toMutableList()
            tags = dto.tags.toMutableList()
            this.calendar = calendar
        }
        timelineService.persist(event)
        return event.toDto()
    }
}

fun CalendarSystem.toDto() = CalendarDTO(
    id,
    name ?: "",
    epochLabel,
    months,
    leapRule,
    setting?.id ?: error("Setting is null"),
)

fun TimelineEvent.toDto() = TimelineEventDTO(
    id,
    calendar?.id ?: error("Calendar is null"),
    title ?: "",
    description,
    startDay ?: 0,
    endDay,
    objectRefs.map { it.id ?: error("id") },
    tags.toList(),
)
