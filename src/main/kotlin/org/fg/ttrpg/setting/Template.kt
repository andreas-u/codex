package org.fg.ttrpg.setting

import java.time.Instant
import java.util.UUID

class Template {
    var id: UUID? = null
    var title: String? = null
    var description: String? = null
    var createdAt: Instant? = null
    /** Type of objects described by this template (e.g. "npc", "item") */
    var type: String? = null
    /** JSON schema describing objects of this template */
    var jsonSchema: String? = null
    var genre: org.fg.ttrpg.genre.Genre? = null
    var gm: org.fg.ttrpg.account.GM? = null
}
