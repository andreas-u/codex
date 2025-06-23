package org.fg.ttrpg.campaign

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import java.util.UUID

@Entity
class Campaign  {
    @Id
    var id: UUID? = null
    var name: String? = null

    @ManyToOne
    var gm: org.fg.ttrpg.account.GM? = null

    @ManyToOne
    var setting: org.fg.ttrpg.setting.Setting? = null

    @OneToMany(mappedBy = "campaign")
    var objects: MutableList<CampaignObject> = mutableListOf()
}
