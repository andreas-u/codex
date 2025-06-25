package org.fg.ttrpg.common.dto

import java.util.UUID

data class GrantDTO(
    val id: UUID?,
    val userId: UUID,
    val objectId: UUID,
    val permissionCode: String,
    val grantedBy: UUID
)
