package org.fg.ttrpg.calendar

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.fg.ttrpg.infra.calendar.CalendarMath
import org.fg.ttrpg.infra.validation.MonthSchemaValidator
import java.util.UUID

@ApplicationScoped
class CalendarService @Inject constructor(
    private val repository: CalendarSystemRepository,
    private val monthValidator: MonthSchemaValidator,
) {

    fun listBySetting(settingId: UUID): List<CalendarSystem> =
        repository.listBySetting(settingId)

    fun findById(id: UUID): CalendarSystem? = repository.findById(id)

    fun findByIdForSetting(id: UUID, settingId: UUID): CalendarSystem? =
        repository.findByIdForSetting(id, settingId)

    fun persist(system: CalendarSystem) {
        system.months?.let { monthValidator.validate(it) }
        repository.persist(system)
    }

    /** Calculate the total number of days in one year. */
    fun totalDays(system: CalendarSystem): Int =
        CalendarMath.totalDays(system.months)

    /** Validate that a day-of-year exists in the calendar. */
    fun isValidDay(system: CalendarSystem, day: Int): Boolean =
        CalendarMath.isValidDay(system.months, day)
}
