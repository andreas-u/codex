package org.fg.ttrpg.genre

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jdbi.v3.core.Jdbi

@ApplicationScoped
class GenreRepository @Inject constructor(private val jdbi: Jdbi) {
    fun insert(genre: Genre) {
        jdbi.useHandle<Nothing> { handle ->
            handle.createUpdate("INSERT INTO genre (id, title, code, setting_id) VALUES (:id, :title, :code, :settingId)")
                .bind("id", genre.id)
                .bind("title", genre.title)
                .bind("code", genre.code)
                .bind("settingId", genre.setting?.id)
                .execute()
        }
    }
}
