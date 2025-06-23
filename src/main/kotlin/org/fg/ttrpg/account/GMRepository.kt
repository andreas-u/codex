package org.fg.ttrpg.account

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class GMRepository : PanacheRepositoryBase<GM, UUID>
