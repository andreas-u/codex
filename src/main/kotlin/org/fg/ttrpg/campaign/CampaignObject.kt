package org.fg.ttrpg.campaign

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
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
}
