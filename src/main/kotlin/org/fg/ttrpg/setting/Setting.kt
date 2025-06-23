package org.fg.ttrpg.setting

import java.time.Instant
import java.util.UUID

class Setting {
    var id: UUID? = null
    var title: String? = null
    var description: String? = null
    var createdAt: Instant? = null
    var gm: org.fg.ttrpg.account.GM? = null
    var genres: MutableList<org.fg.ttrpg.genre.Genre> = mutableListOf()
    var objects: MutableList<SettingObject> = mutableListOf()
}
