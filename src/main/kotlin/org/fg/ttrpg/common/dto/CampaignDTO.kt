package org.fg.ttrpg.common.dto

import java.util.UUID

data class CampaignDTO(
    val id: UUID?,
    val name: String,
    val gmId: UUID,
    val settingId: UUID
)
