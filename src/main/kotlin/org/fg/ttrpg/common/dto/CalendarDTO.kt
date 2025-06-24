package org.fg.ttrpg.common.dto

import java.util.UUID

/** DTO for calendar systems. */
data class CalendarDTO(
    val id: UUID?,
    val name: String,
    val epochLabel: String? = null,
    val months: String? = null,
    val leapRule: String? = null,
    val settingId: UUID,
)
