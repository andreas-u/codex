package org.fg.ttrpg.common.dto

data class CampaignObjectDTO(
    val id: Long?,
    val name: String,
    val description: String? = null,
    val campaignId: Long,
    val settingObjectId: Long
)
