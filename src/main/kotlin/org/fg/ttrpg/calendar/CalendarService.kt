package org.fg.ttrpg.calendar

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.UUID

@ApplicationScoped
class CalendarService @Inject constructor(private val repository: CalendarSystemRepository) {
    private val mapper = ObjectMapper()

    fun listBySetting(settingId: UUID): List<CalendarSystem> =
        repository.listBySetting(settingId)

    fun findById(id: UUID): CalendarSystem? = repository.findById(id)

    fun findByIdForSetting(id: UUID, settingId: UUID): CalendarSystem? =
        repository.findByIdForSetting(id, settingId)

    fun persist(system: CalendarSystem) {
        repository.persist(system)
    }

    /** Calculate the total number of days in one year. */
    fun totalDays(system: CalendarSystem): Int =
        parseMonths(system).sumOf { it.days }

    /** Validate that a day-of-year exists in the calendar. */
    fun isValidDay(system: CalendarSystem, day: Int): Boolean {
        val total = totalDays(system)
        return day in 1..total
    }

    private data class Month(val name: String, val days: Int)

    private fun parseMonths(system: CalendarSystem): List<Month> =
        runCatching {
            val node = mapper.readTree(system.months ?: "[]")
            node.map { Month(it.get("name").asText(), it.get("days").asInt()) }
        }.getOrDefault(emptyList())
}
