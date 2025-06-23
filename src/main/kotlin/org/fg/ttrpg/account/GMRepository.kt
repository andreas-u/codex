package org.fg.ttrpg.account

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jooq.DSLContext

@ApplicationScoped
class GMRepository @Inject constructor(private val dsl: DSLContext)
