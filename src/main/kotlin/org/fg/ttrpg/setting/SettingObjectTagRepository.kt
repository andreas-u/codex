package org.fg.ttrpg.setting

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jdbi.v3.core.Jdbi
import java.util.UUID

@ApplicationScoped
class SettingObjectTagRepository @Inject constructor(private val jdbi: Jdbi) {

    fun listBySettingObject(id: UUID): List<String> =
        jdbi.withHandle<List<String>, Exception> { handle ->
            handle.createQuery("SELECT tag FROM setting_object_tags WHERE setting_object_id = :id")
                .bind("id", id)
                .mapTo(String::class.java)
                .list()
        }

    fun replaceForObject(id: UUID, tags: List<String>) {
        jdbi.useHandle<Exception> { handle ->
            handle.createUpdate("DELETE FROM setting_object_tags WHERE setting_object_id = :id")
                .bind("id", id)
                .execute()
            tags.forEach { tag ->
                handle.createUpdate("INSERT INTO setting_object_tags (setting_object_id, tag) VALUES (:id, :tag)")
                    .bind("id", id)
                    .bind("tag", tag)
                    .execute()
            }
        }
    }
}
