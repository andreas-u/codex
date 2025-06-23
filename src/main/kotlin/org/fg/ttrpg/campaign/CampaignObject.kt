package org.fg.ttrpg.campaign

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne

@Entity
class CampaignObject : PanacheEntity() {
    lateinit var name: String
    var description: String? = null

    @ManyToOne
    lateinit var campaign: Campaign

    @ManyToOne
    lateinit var settingObject: org.fg.ttrpg.setting.SettingObject
}
