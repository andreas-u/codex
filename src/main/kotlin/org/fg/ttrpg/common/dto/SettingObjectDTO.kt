package org.fg.ttrpg.common.dto

data class SettingObjectDTO(
    val id: Long?,
    val name: String,
    val description: String? = null,
    val settingId: Long
)
