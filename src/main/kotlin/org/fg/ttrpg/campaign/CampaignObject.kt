package org.fg.ttrpg.campaign

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.util.UUID

@Entity
class CampaignObject {
    @Id
    var id: UUID? = null
    var name: String? = null
    var description: String? = null

    @ManyToOne
    var campaign: Campaign? = null

    @ManyToOne
    var settingObject: org.fg.ttrpg.setting.SettingObject? = null

    @ManyToOne
    var gm: org.fg.ttrpg.account.GM? = null
}
