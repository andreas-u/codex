package org.fg.ttrpg.setting

import java.time.Instant
import java.util.UUID

class SettingObject {
    var id: UUID? = null
    var slug: String? = null
    var title: String? = null
    var description: String? = null
    /** Arbitrary JSON payload describing this object */
    var payload: String? = null
    var createdAt: Instant? = null
    var tags: MutableList<String> = mutableListOf()
    var setting: Setting? = null
    var gm: org.fg.ttrpg.account.GM? = null
    var template: Template? = null
}
