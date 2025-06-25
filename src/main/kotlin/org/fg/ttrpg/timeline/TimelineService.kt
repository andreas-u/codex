package org.fg.ttrpg.timeline

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.UUID

@ApplicationScoped
class TimelineService @Inject constructor(
    private val repository: TimelineEventRepository,
    private val usageRepository: TimelineObjectUsageRepository,
) {
    fun listByCalendar(calendarId: UUID): List<TimelineEvent> =
        repository.listByCalendar(calendarId)

    fun findById(id: UUID): TimelineEvent? = repository.findById(id)

    fun findByIdForCalendar(id: UUID, calendarId: UUID): TimelineEvent? =
        repository.findByIdForCalendar(id, calendarId)

    fun persist(event: TimelineEvent) {
        repository.persist(event)
    }

    fun listObjectsForEvent(eventId: UUID): List<TimelineObjectUsage> =
        usageRepository.listObjectsForEvent(eventId)

    fun listEventsForObject(objectId: UUID): List<TimelineObjectUsage> =
        usageRepository.listEventsForObject(objectId)
}
