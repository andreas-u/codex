package org.fg.ttrpg.genre

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jooq.DSLContext

@ApplicationScoped
class GenreRepository @Inject constructor(private val dsl: DSLContext)
