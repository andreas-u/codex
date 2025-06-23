package org.fg.ttrpg.setting

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class TemplateRepository : PanacheRepositoryBase<Template, UUID> {
    fun listByGm(gmId: UUID) = list("gm.id", gmId)

    fun listBySettingAndGm(settingId: UUID, gmId: UUID) =
        list("setting.id=?1 and gm.id=?2", settingId, gmId)

    fun listByGenre(genre: String) =
        list("setting.genres.title", genre)

    fun listByType(type: String) =
        list("title", type)

    fun listByGenreAndType(genreId: UUID, type: String) : List<Template> =
        list("genre.id = ?1 and type = ?2", genreId, type)
}
