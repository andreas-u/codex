package org.fg.ttrpg.campaign

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class CampaignRepository : PanacheRepositoryBase<Campaign, UUID> {
    fun listByGm(gmId: UUID) = list("gm.id", gmId)

    fun findByIdForGm(id: UUID, gmId: UUID): Campaign? =
        find("id=?1 and gm.id=?2", id, gmId).firstResult()
}
