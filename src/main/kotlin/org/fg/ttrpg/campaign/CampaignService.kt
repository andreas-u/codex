package org.fg.ttrpg.campaign

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.UUID

@ApplicationScoped
class CampaignService @Inject constructor(private val repository: CampaignRepository) {
    fun listAll(): List<Campaign> = repository.listAll()

    fun findById(id: UUID): Campaign? = repository.findById(id)

    fun persist(campaign: Campaign) {
        repository.persist(campaign)
    }
}
