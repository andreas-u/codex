package org.fg.ttrpg.genre

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class GenreRepository : PanacheRepository<Genre>
