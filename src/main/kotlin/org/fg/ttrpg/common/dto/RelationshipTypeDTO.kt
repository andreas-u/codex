package org.fg.ttrpg.common.dto

import java.util.UUID

data class RelationshipTypeDTO(
    val id: UUID?,
    val code: String,
    val displayName: String,
    val directional: Boolean,
    val schemaJson: String? = null,
    val settingId: UUID?
)
