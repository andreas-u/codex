package org.fg.ttrpg.infra

import org.fg.ttrpg.infra.calendar.CalendarMath
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CalendarMathTest {
    @Test
    fun `totalDays sums month lengths`() {
        val months = """[{"name":"A","days":30},{"name":"B","days":15}]"""
        CalendarMath.totalDays(months) shouldBe 45
    }

    @Test
    fun `isValidDay checks range`() {
        val months = """[{"name":"A","days":30}]"""
        CalendarMath.isValidDay(months, 31) shouldBe false
        CalendarMath.isValidDay(months, 30) shouldBe true
    }
}
