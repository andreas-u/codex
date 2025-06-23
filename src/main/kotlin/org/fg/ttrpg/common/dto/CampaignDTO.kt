package org.fg.ttrpg.common.dto

data class CampaignDTO(
    val id: Long?,
    val name: String,
    val gmId: Long,
    val settingId: Long
)
