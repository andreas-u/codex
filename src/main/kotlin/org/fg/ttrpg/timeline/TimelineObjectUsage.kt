package org.fg.ttrpg.timeline

import java.util.UUID

/** Mapping of a timeline event to a referenced setting object. */
data class TimelineObjectUsage(
    val eventId: UUID,
    val settingObjectId: UUID,
)
