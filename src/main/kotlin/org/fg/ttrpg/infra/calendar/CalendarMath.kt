package org.fg.ttrpg.infra.calendar

import com.fasterxml.jackson.databind.ObjectMapper

/** Utility functions for dealing with calendar month schemas. */
object CalendarMath {
    private val mapper = ObjectMapper()

    data class Month(val name: String, val days: Int)

    /** Parse month definitions from a JSON array. */
    fun parseMonths(json: String?): List<Month> =
        runCatching {
            val node = mapper.readTree(json ?: "[]")
            node.map { Month(it.get("name").asText(), it.get("days").asInt()) }
        }.getOrDefault(emptyList())

    /** Calculate the total number of days in a year. */
    fun totalDays(json: String?): Int = parseMonths(json).sumOf { it.days }

    /** Validate that a day-of-year exists for the given months. */
    fun isValidDay(json: String?, day: Int): Boolean {
        val total = totalDays(json)
        return day in 1..total
    }
}
