package org.fg.ttrpg.common.dto

import java.util.UUID

data class RelationshipDTO(
    val id: UUID?,
    val settingId: UUID,
    val typeId: UUID,
    val sourceObject: UUID,
    val targetObject: UUID,
    val isBidirectional: Boolean = false,
    val properties: String? = null
)
