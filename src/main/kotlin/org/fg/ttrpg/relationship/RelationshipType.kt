package org.fg.ttrpg.relationship

import java.time.Instant
import java.util.UUID

class RelationshipType {
    var id: UUID? = null
    var setting: org.fg.ttrpg.setting.Setting? = null
    var code: String? = null
    var displayName: String? = null
    var directional: Boolean = false
    var schemaJson: String? = null
    var createdAt: Instant? = null
}
