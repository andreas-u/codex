package org.fg.ttrpg.timeline

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.fg.ttrpg.calendar.CalendarSystem
import org.fg.ttrpg.setting.SettingObject
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.util.UUID

@ApplicationScoped
class TimelineEventRepository @Inject constructor(private val jdbi: Jdbi) {

    fun listByCalendar(calendarId: UUID): List<TimelineEvent> =
        jdbi.withHandle<List<TimelineEvent>, Exception> { handle ->
            handle.createQuery(
                "SELECT id, calendar_id, title, description, start_day, end_day, object_refs, tags FROM timeline_event WHERE calendar_id = :calendarId"
            )
                .bind("calendarId", calendarId)
                .map(TimelineEventMapper())
                .list()
        }

    fun findById(id: UUID): TimelineEvent? =
        jdbi.withHandle<TimelineEvent?, Exception> { handle ->
            handle.createQuery(
                "SELECT id, calendar_id, title, description, start_day, end_day, object_refs, tags FROM timeline_event WHERE id = :id"
            )
                .bind("id", id)
                .map(TimelineEventMapper())
                .findOne()
                .orElse(null)
        }

    fun findByIdForCalendar(id: UUID, calendarId: UUID): TimelineEvent? =
        jdbi.withHandle<TimelineEvent?, Exception> { handle ->
            handle.createQuery(
                "SELECT id, calendar_id, title, description, start_day, end_day, object_refs, tags FROM timeline_event WHERE id = :id AND calendar_id = :calendarId"
            )
                .bind("id", id)
                .bind("calendarId", calendarId)
                .map(TimelineEventMapper())
                .findOne()
                .orElse(null)
        }

    fun persist(event: TimelineEvent) {
        if (event.id == null) {
            event.id = UUID.randomUUID()
        }
        jdbi.useHandle<Exception> { handle ->
            handle.createUpdate(
                "INSERT INTO timeline_event (id, calendar_id, title, description, start_day, end_day, object_refs, tags) VALUES (:id, :calendarId, :title, :description, :startDay, :endDay, :objectRefs, :tags)"
            )
                .bind("id", event.id)
                .bind("calendarId", event.calendar?.id)
                .bind("title", event.title)
                .bind("description", event.description)
                .bind("startDay", event.startDay)
                .bind("endDay", event.endDay)
                .bind("objectRefs", event.objectRefs.takeIf { it.isNotEmpty() }?.toTypedArray())
                .bind("tags", event.tags.takeIf { it.isNotEmpty() }?.toTypedArray())
                .execute()
        }
    }

    private class TimelineEventMapper : RowMapper<TimelineEvent> {
        override fun map(rs: ResultSet, ctx: StatementContext): TimelineEvent = TimelineEvent().apply {
            id = rs.getObject("id", UUID::class.java)
            calendar = CalendarSystem().apply { id = rs.getObject("calendar_id", UUID::class.java) }
            title = rs.getString("title")
            description = rs.getString("description")
            startDay = rs.getInt("start_day")
            val end = rs.getInt("end_day")
            if (!rs.wasNull()) {
                endDay = end
            }
            val objArray = rs.getArray("object_refs")
            if (objArray != null) {
                @Suppress("UNCHECKED_CAST")
                val arr = objArray.array as Array<Any>
                objectRefs = arr.map { SettingObject().apply { id = UUID.fromString(it.toString()) } }.toMutableList()
            }
            val tagArray = rs.getArray("tags")
            if (tagArray != null) {
                @Suppress("UNCHECKED_CAST")
                val arr = tagArray.array as Array<Any>
                tags = arr.map { it.toString() }.toMutableList()
            }
        }
    }
}
