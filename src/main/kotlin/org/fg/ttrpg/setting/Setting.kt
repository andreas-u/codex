package org.fg.ttrpg.setting

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Entity
import jakarta.persistence.Id

import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne

import jakarta.persistence.OneToMany
import java.util.UUID

@Entity
class Setting  {
    @Id
    var id: UUID? = null
    var name: String? = null
    var description: String? = null


    @ManyToOne
    var gm: org.fg.ttrpg.account.GM? = null


    @OneToMany(mappedBy = "setting")

    var genres: MutableList<org.fg.ttrpg.genre.Genre> = mutableListOf()

    @OneToMany(mappedBy = "setting")
    var objects: MutableList<SettingObject> = mutableListOf()
}
