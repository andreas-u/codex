package org.fg.ttrpg.campaign

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class CampaignService @Inject constructor(private val repository: CampaignRepository) {
    fun listAll(): List<Campaign> = repository.listAll()

    fun findById(id: Long): Campaign? = repository.findById(id)

    fun persist(campaign: Campaign) {
        repository.persist(campaign)
    }
}
