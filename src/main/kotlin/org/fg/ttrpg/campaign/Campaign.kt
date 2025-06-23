package org.fg.ttrpg.campaign

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany

@Entity
class Campaign : PanacheEntity() {
    lateinit var name: String

    @ManyToOne
    lateinit var gm: org.fg.ttrpg.account.GM

    @ManyToOne
    lateinit var setting: org.fg.ttrpg.setting.Setting

    @OneToMany(mappedBy = "campaign")
    var objects: MutableList<CampaignObject> = mutableListOf()
}
