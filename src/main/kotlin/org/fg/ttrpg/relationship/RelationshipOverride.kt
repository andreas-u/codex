package org.fg.ttrpg.relationship

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "relationship_override")
class RelationshipOverride {
    @Id
    var id: UUID? = null

    @ManyToOne
    var campaign: org.fg.ttrpg.campaign.Campaign? = null

    @ManyToOne
    @JoinColumn(name = "base_relationship")
    var baseRelationship: Relationship? = null

    @Enumerated(EnumType.STRING)
    var overrideMode: OverrideMode? = null

    @Column(columnDefinition = "jsonb")
    var properties: String? = null

    @Column(name = "created_at")
    var createdAt: Instant? = null
}

enum class OverrideMode { PATCH, REPLACE, DELETE }
