package org.fg.ttrpg.campaign

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class CampaignObjectRepository : PanacheRepositoryBase<CampaignObject, UUID> {
    fun findByIdForGm(id: UUID, gmId: UUID): CampaignObject? =
        find("id=?1 and gm.id=?2", id, gmId).firstResult()
}
