package org.fg.ttrpg.common.dto

import java.util.UUID

/** DTO for campaign-specific timeline event overrides. */
data class CampaignEventOverrideDTO(
    val id: UUID?,
    val campaignId: UUID,
    val baseEventId: UUID?,
    val overrideMode: String,
    val payload: String? = null,
)
