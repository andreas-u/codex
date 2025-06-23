package org.fg.ttrpg.account

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import java.util.UUID

@Entity
class GM{
    @Id
    var id: UUID? = null
    var username: String? = null
    var email: String? = null

    @OneToMany(mappedBy = "gm")
    var campaigns: MutableList<org.fg.ttrpg.campaign.Campaign> = mutableListOf()
}
