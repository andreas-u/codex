package org.fg.ttrpg.account

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany

@Entity
class GM : PanacheEntity() {
    lateinit var username: String
    var email: String? = null

    @OneToMany(mappedBy = "gm")
    var campaigns: MutableList<org.fg.ttrpg.campaign.Campaign> = mutableListOf()
}
