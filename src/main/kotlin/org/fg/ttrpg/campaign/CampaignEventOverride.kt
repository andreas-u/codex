package org.fg.ttrpg.campaign

import org.fg.ttrpg.timeline.TimelineEvent
import java.time.Instant
import java.util.UUID

class CampaignEventOverride {
    var id: UUID? = null
    var campaign: Campaign? = null
    var baseEvent: TimelineEvent? = null
    var overrideMode: OverrideMode? = null
    var payload: String? = null
    var createdAt: Instant? = null
}

enum class OverrideMode { PATCH, REPLACE, DELETE }
