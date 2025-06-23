package org.fg.ttrpg.common.dto

data class TemplateDTO(
    val id: Long?,
    val name: String,
    val description: String? = null,
    val schema: String? = null,
    val settingId: Long
)
