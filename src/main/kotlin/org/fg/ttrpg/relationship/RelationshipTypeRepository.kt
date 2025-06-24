package org.fg.ttrpg.relationship

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jdbi.v3.core.Jdbi

@ApplicationScoped
class RelationshipTypeRepository @Inject constructor(private val jdbi: Jdbi)
