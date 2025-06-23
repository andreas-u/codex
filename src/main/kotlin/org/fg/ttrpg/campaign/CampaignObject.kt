package org.fg.ttrpg.campaign

import java.time.Instant
import java.util.UUID

class CampaignObject {
    var id: UUID? = null
    var title: String? = null
    var description: String? = null
    var campaign: Campaign? = null
    var settingObject: org.fg.ttrpg.setting.SettingObject? = null
    var template: org.fg.ttrpg.setting.Template? = null
    var overrideMode: String? = null
    /** JSON payload storing object data */
    var payload: String? = null
    var createdAt: Instant? = null
    var gm: org.fg.ttrpg.account.GM? = null
}
