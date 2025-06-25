package org.fg.ttrpg.timeline

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.util.UUID

@ApplicationScoped
class TimelineObjectUsageRepository @Inject constructor(private val jdbi: Jdbi) {

    fun listObjectsForEvent(eventId: UUID): List<TimelineObjectUsage> =
        jdbi.withHandle<List<TimelineObjectUsage>, Exception> { handle ->
            handle.createQuery(
                "SELECT event_id, setting_object_id FROM timeline_object_usage WHERE event_id = :eid"
            )
                .bind("eid", eventId)
                .map(TimelineObjectUsageMapper())
                .list()
        }

    fun listEventsForObject(objectId: UUID): List<TimelineObjectUsage> =
        jdbi.withHandle<List<TimelineObjectUsage>, Exception> { handle ->
            handle.createQuery(
                "SELECT event_id, setting_object_id FROM timeline_object_usage WHERE setting_object_id = :oid"
            )
                .bind("oid", objectId)
                .map(TimelineObjectUsageMapper())
                .list()
        }

    private class TimelineObjectUsageMapper : RowMapper<TimelineObjectUsage> {
        override fun map(rs: ResultSet, ctx: StatementContext): TimelineObjectUsage =
            TimelineObjectUsage(
                rs.getObject("event_id", UUID::class.java),
                rs.getObject("setting_object_id", UUID::class.java),
            )
    }
}
