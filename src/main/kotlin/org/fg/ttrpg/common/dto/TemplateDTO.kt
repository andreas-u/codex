package org.fg.ttrpg.common.dto

import java.util.UUID

data class TemplateDTO(
    val id: UUID?,
    val title: String,
    val description: String? = null,
    val type: String,
    val jsonSchema: String? = null,
    val genreId: UUID
)
