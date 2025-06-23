package org.fg.ttrpg.campaign

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class CampaignRepository : PanacheRepository<Campaign>
