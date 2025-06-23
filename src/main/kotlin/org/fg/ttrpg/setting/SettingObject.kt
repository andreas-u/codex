package org.fg.ttrpg.setting

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
class SettingObject  {
    @Id
    var id: UUID? = null
    var slug: String? = null
    @Column(name = "name")
    var title: String? = null
    var description: String? = null
    /** Arbitrary JSON payload describing this object */
    @Column(columnDefinition = "jsonb")
    var payload: String? = null

    @Column(name = "created_at")
    var createdAt: Instant? = null

    @ElementCollection
    @CollectionTable(name = "setting_object_tags", joinColumns = [JoinColumn(name = "setting_object_id")])
    @Column(name = "tag")
    var tags: MutableList<String> = mutableListOf()

    @ManyToOne
    var setting: Setting? = null

    @ManyToOne
    var gm: org.fg.ttrpg.account.GM? = null

    @ManyToOne
    var template: Template? = null
}
