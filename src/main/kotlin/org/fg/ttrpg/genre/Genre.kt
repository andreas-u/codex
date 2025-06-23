package org.fg.ttrpg.genre

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import jakarta.persistence.Entity
import jakarta.persistence.ManyToMany

@Entity
class Genre : PanacheEntity() {
    lateinit var name: String

    @ManyToMany(mappedBy = "genres")
    var settings: MutableList<org.fg.ttrpg.setting.Setting> = mutableListOf()
}
