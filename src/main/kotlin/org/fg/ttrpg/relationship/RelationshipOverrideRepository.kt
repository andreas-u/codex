package org.fg.ttrpg.relationship

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class RelationshipOverrideRepository : PanacheRepositoryBase<RelationshipOverride, UUID>
