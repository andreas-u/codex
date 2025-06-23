package org.fg.ttrpg.common.dto

import java.util.UUID

data class TemplateDTO(
    val id: UUID?,
    val name: String,
    val description: String? = null,
    val schema: String? = null,
    val settingId: UUID
)
