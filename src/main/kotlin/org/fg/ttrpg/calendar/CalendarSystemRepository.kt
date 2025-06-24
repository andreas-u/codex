package org.fg.ttrpg.calendar

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.fg.ttrpg.setting.Setting
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.util.UUID

@ApplicationScoped
class CalendarSystemRepository @Inject constructor(private val jdbi: Jdbi) {

    fun listBySetting(settingId: UUID): List<CalendarSystem> =
        jdbi.withHandle<List<CalendarSystem>, Exception> { handle ->
            handle.createQuery(
                "SELECT id, setting_id, name, epoch_label, months, leap_rule, created_at FROM calendar_system WHERE setting_id = :settingId"
            )
                .bind("settingId", settingId)
                .map(CalendarSystemMapper())
                .list()
        }

    fun findById(id: UUID): CalendarSystem? =
        jdbi.withHandle<CalendarSystem?, Exception> { handle ->
            handle.createQuery(
                "SELECT id, setting_id, name, epoch_label, months, leap_rule, created_at FROM calendar_system WHERE id = :id"
            )
                .bind("id", id)
                .map(CalendarSystemMapper())
                .findOne()
                .orElse(null)
        }

    fun findByIdForSetting(id: UUID, settingId: UUID): CalendarSystem? =
        jdbi.withHandle<CalendarSystem?, Exception> { handle ->
            handle.createQuery(
                "SELECT id, setting_id, name, epoch_label, months, leap_rule, created_at FROM calendar_system WHERE id = :id AND setting_id = :settingId"
            )
                .bind("id", id)
                .bind("settingId", settingId)
                .map(CalendarSystemMapper())
                .findOne()
                .orElse(null)
        }

    fun persist(system: CalendarSystem) {
        if (system.id == null) {
            system.id = UUID.randomUUID()
        }
        jdbi.useHandle<Exception> { handle ->
            if (system.createdAt != null) {
                handle.createUpdate(
                    "INSERT INTO calendar_system (id, setting_id, name, epoch_label, months, leap_rule, created_at) VALUES (:id, :settingId, :name, :epochLabel, :months::jsonb, :leapRule::jsonb, :createdAt)"
                )
                    .bind("id", system.id)
                    .bind("settingId", system.setting?.id)
                    .bind("name", system.name)
                    .bind("epochLabel", system.epochLabel)
                    .bind("months", system.months)
                    .bind("leapRule", system.leapRule)
                    .bind("createdAt", system.createdAt)
                    .execute()
            } else {
                handle.createUpdate(
                    "INSERT INTO calendar_system (id, setting_id, name, epoch_label, months, leap_rule) VALUES (:id, :settingId, :name, :epochLabel, :months::jsonb, :leapRule::jsonb)"
                )
                    .bind("id", system.id)
                    .bind("settingId", system.setting?.id)
                    .bind("name", system.name)
                    .bind("epochLabel", system.epochLabel)
                    .bind("months", system.months)
                    .bind("leapRule", system.leapRule)
                    .execute()
            }
        }
    }

    private class CalendarSystemMapper : RowMapper<CalendarSystem> {
        override fun map(rs: ResultSet, ctx: StatementContext): CalendarSystem = CalendarSystem().apply {
            id = rs.getObject("id", UUID::class.java)
            setting = Setting().apply { id = rs.getObject("setting_id", UUID::class.java) }
            name = rs.getString("name")
            epochLabel = rs.getString("epoch_label")
            months = rs.getString("months")
            leapRule = rs.getString("leap_rule")
            val ts = rs.getTimestamp("created_at")
            if (ts != null) {
                createdAt = ts.toInstant()
            }
        }
    }
}
