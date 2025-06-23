package org.fg.ttrpg.genre

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class GenreRepository : PanacheRepositoryBase<Genre, UUID>
