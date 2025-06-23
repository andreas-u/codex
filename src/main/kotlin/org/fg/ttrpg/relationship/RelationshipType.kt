package org.fg.ttrpg.relationship

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "relationship_type")
class RelationshipType {
    @Id
    var id: UUID? = null

    @ManyToOne
    var setting: org.fg.ttrpg.setting.Setting? = null

    @Column(nullable = false, unique = true)
    var code: String? = null

    @Column(name = "display_name")
    var displayName: String? = null

    @Column(nullable = false)
    var directional: Boolean = false

    @Column(name = "schema_json", columnDefinition = "jsonb")
    var schemaJson: String? = null

    @Column(name = "created_at")
    var createdAt: Instant? = null
}
