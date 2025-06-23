package org.fg.ttrpg.setting

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.*
import java.util.UUID

@Entity
class SettingObject  {
    @Id
    var id: UUID? = null
    var slug: String? = null
    var name: String? = null
    var description: String? = null
    /** Arbitrary JSON payload describing this object */
    @Column(columnDefinition = "jsonb")
    var payload: String? = null

    @ElementCollection
    @CollectionTable(name = "setting_object_tags", joinColumns = [JoinColumn(name = "setting_object_id")])
    @Column(name = "tag")
    var tags: MutableList<String> = mutableListOf()

    @ManyToOne
    var setting: Setting? = null

    @ManyToOne
    var template: Template? = null
}
