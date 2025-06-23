package org.fg.ttrpg.common.dto

import java.util.UUID

data class RelationshipOverrideDTO(
    val id: UUID?,
    val campaignId: UUID,
    val baseRelationship: UUID?,
    val overrideMode: String,
    val properties: String? = null
)
