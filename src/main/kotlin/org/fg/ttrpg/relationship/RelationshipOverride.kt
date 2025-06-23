package org.fg.ttrpg.relationship

import java.time.Instant
import java.util.UUID

class RelationshipOverride {
    var id: UUID? = null
    var campaign: org.fg.ttrpg.campaign.Campaign? = null
    var baseRelationship: Relationship? = null
    var overrideMode: OverrideMode? = null
    var properties: String? = null
    var createdAt: Instant? = null
}

enum class OverrideMode { PATCH, REPLACE, DELETE }
