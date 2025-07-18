package org.fg.ttrpg.common.dto

import java.util.UUID

data class CampaignObjectDTO(
    val id: UUID?,
    val title: String,
    val description: String? = null,
    val campaignId: UUID,
    val settingObjectId: UUID
)
