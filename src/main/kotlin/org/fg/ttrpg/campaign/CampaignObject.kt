package org.fg.ttrpg.campaign

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.time.Instant
import java.util.UUID

@Entity
class CampaignObject {
    @Id
    var id: UUID? = null
    @Column(name = "name")
    var title: String? = null
    var description: String? = null

    @ManyToOne
    var campaign: Campaign? = null

    @ManyToOne
    var settingObject: org.fg.ttrpg.setting.SettingObject? = null

    @ManyToOne
    var template: org.fg.ttrpg.setting.Template? = null

    var overrideMode: String? = null

    /** JSON payload storing object data */
    var payload: String? = null

    @Column(name = "created_at")
    var createdAt: Instant? = null

    @ManyToOne
    var gm: org.fg.ttrpg.account.GM? = null
}
