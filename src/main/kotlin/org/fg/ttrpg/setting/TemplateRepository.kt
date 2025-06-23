package org.fg.ttrpg.setting

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class TemplateRepository : PanacheRepositoryBase<Template, UUID> {
    fun listByGenre(genre: String) =
        list("setting.genres.name", genre)

    fun listByType(type: String) =
        list("name", type)
}
