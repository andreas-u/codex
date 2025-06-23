package org.fg.ttrpg.common.dto

data class SettingDTO(
    val id: Long?,
    val name: String,
    val description: String? = null
)
