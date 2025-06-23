package org.fg.ttrpg.common.dto

import java.util.UUID

data class SettingObjectDTO(
    val id: UUID?,
    val name: String,
    val description: String? = null,
    val settingId: UUID
)
