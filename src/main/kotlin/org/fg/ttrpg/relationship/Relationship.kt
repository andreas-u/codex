package org.fg.ttrpg.relationship

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "relationship")
class Relationship {
    @Id
    var id: UUID? = null

    @ManyToOne
    var setting: org.fg.ttrpg.setting.Setting? = null

    @ManyToOne
    var type: RelationshipType? = null

    @ManyToOne
    @JoinColumn(name = "source_object")
    var sourceObject: org.fg.ttrpg.setting.SettingObject? = null

    @ManyToOne
    @JoinColumn(name = "target_object")
    var targetObject: org.fg.ttrpg.setting.SettingObject? = null

    @Column(name = "is_bidirectional")
    var isBidirectional: Boolean = false

    @Column(columnDefinition = "jsonb")
    var properties: String? = null

    @Column(name = "created_at")
    var createdAt: Instant? = null
}
