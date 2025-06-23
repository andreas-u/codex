package org.fg.ttrpg.common.dto

import java.util.UUID

data class SettingObjectDTO(
    val id: UUID?,
    val slug: String,
    val title: String,
    val description: String? = null,
    val payload: String? = null,
    val tags: List<String> = emptyList(),
    val settingId: UUID,
    val templateId: UUID? = null
)
