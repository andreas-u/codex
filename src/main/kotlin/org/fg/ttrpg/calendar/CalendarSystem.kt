package org.fg.ttrpg.calendar

import java.time.Instant
import java.util.UUID

class CalendarSystem {
    var id: UUID? = null
    var name: String? = null
    var epochLabel: String? = null
    /** JSON description of months and their day counts */
    var months: String? = null
    /** JSON description of leap year rules */
    var leapRule: String? = null
    var createdAt: Instant? = null
    var setting: org.fg.ttrpg.setting.Setting? = null
}
