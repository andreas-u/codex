package org.fg.ttrpg.setting

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne

@Entity
class Template : PanacheEntity() {
    lateinit var name: String
    var description: String? = null

    /** JSON schema describing objects of this template */
    var schema: String? = null

    @ManyToOne
    lateinit var setting: Setting
}
