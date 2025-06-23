package org.fg.ttrpg.setting

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class TemplateRepository : PanacheRepositoryBase<Template, UUID> {
    fun listByGenreAndType(genreId: UUID, type: String): List<Template> =
        list("genre.id = ?1 and type = ?2", genreId, type)
}
