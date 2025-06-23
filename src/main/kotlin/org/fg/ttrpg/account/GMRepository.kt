package org.fg.ttrpg.account

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class GMRepository : PanacheRepository<GM>
