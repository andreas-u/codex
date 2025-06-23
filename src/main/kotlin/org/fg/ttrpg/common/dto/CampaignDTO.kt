package org.fg.ttrpg.common.dto

import java.util.UUID

data class CampaignDTO(
    val id: UUID?,
    val title: String,
    val gmId: UUID,
    val settingId: UUID
)
