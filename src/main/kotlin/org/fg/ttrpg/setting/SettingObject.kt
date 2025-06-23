package org.fg.ttrpg.setting

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne

@Entity
class SettingObject : PanacheEntity() {
    lateinit var name: String
    var description: String? = null

    @ManyToOne
    lateinit var setting: Setting
}
