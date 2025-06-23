package org.fg.ttrpg.campaign

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.UUID

@ApplicationScoped
class CampaignService @Inject constructor(private val repository: CampaignRepository) {
    fun listAll(gmId: UUID): List<Campaign> = repository.listByGm(gmId)

    fun findById(id: UUID): Campaign? = repository.findById(id)

    fun findByIdForGm(id: UUID, gmId: UUID): Campaign? = repository.findByIdForGm(id, gmId)

    fun persist(campaign: Campaign) {
        repository.persist(campaign)
    }
}
