package org.fg.ttrpg.relationship

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class RelationshipTypeRepository : PanacheRepositoryBase<RelationshipType, UUID>
