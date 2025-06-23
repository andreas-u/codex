package org.fg.ttrpg.campaign

import java.time.Instant
import java.util.UUID

class Campaign {
    var id: UUID? = null
    var title: String? = null
    var startedOn: Instant? = null
    var gm: org.fg.ttrpg.account.GM? = null
    var setting: org.fg.ttrpg.setting.Setting? = null
    var objects: MutableList<CampaignObject> = mutableListOf()
}
