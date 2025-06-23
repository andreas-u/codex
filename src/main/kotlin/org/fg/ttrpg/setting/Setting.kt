package org.fg.ttrpg.setting

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import jakarta.persistence.Entity
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany

@Entity
class Setting : PanacheEntity() {
    lateinit var name: String
    var description: String? = null

    @ManyToMany
    var genres: MutableList<org.fg.ttrpg.genre.Genre> = mutableListOf()

    @OneToMany(mappedBy = "setting")
    var templates: MutableList<Template> = mutableListOf()

    @OneToMany(mappedBy = "setting")
    var objects: MutableList<SettingObject> = mutableListOf()
}
