package org.fg.ttrpg.relationship

import java.time.Instant
import java.util.UUID

class Relationship {
    var id: UUID? = null
    var setting: org.fg.ttrpg.setting.Setting? = null
    var type: RelationshipType? = null
    var sourceObject: org.fg.ttrpg.setting.SettingObject? = null
    var targetObject: org.fg.ttrpg.setting.SettingObject? = null
    var isBidirectional: Boolean = false
    var properties: String? = null
    var createdAt: Instant? = null
}
