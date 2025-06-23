package org.fg.ttrpg.relationship

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jooq.DSLContext

@ApplicationScoped
class RelationshipOverrideRepository @Inject constructor(private val dsl: DSLContext)
