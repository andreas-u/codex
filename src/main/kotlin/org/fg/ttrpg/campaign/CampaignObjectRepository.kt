package org.fg.ttrpg.campaign

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class CampaignObjectRepository : PanacheRepositoryBase<CampaignObject, UUID> {
    fun listByCampaignAndGm(campaignId: UUID, gmId: UUID) =
        list("campaign.id=?1 and gm.id=?2", campaignId, gmId)

    fun findByIdForGm(id: UUID, gmId: UUID): CampaignObject? =
        find("id=?1 and gm.id=?2", id, gmId).firstResult()
}
