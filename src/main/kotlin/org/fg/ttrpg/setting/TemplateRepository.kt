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
class TemplateRepository : PanacheRepositoryBase<Template, UUID> {
    fun listByGm(gmId: UUID) = list("gm.id", gmId)

    fun listBySettingAndGm(settingId: UUID, gmId: UUID) =
        list("setting.id=?1 and gm.id=?2", settingId, gmId)
}
class TemplateRepository : PanacheRepositoryBase<Template, UUID> {
    fun listByGenreAndType(genreId: UUID, type: String): List<Template> =
        list("genre.id = ?1 and type = ?2", genreId, type)
}
