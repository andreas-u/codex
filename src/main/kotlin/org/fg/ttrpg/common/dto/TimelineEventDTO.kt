package org.fg.ttrpg.common.dto

import java.util.UUID

/** DTO for timeline events. */
data class TimelineEventDTO(
    val id: UUID?,
    val calendarId: UUID,
    val title: String,
    val description: String? = null,
    val startDay: Int,
    val endDay: Int? = null,
    val objectRefs: List<UUID> = emptyList(),
    val tags: List<String> = emptyList(),
)
