package org.fg.ttrpg.common.dto

import java.util.UUID

data class SettingDTO(
    val id: UUID?,
    val name: String,
    val description: String? = null,
    val gmId: UUID
)
