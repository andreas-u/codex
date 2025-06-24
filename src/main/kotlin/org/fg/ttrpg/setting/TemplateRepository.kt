package org.fg.ttrpg.setting

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.fg.ttrpg.account.GM
import org.fg.ttrpg.genre.Genre
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class TemplateRepository @Inject constructor(private val jdbi: Jdbi) {

    fun listByGm(gmId: UUID): List<Template> =
        jdbi.withHandle<List<Template>, Exception> { handle ->
            handle.createQuery("SELECT id, title, description, json_schema, type, gm_id, genre_id, created_at FROM template WHERE gm_id = :gmId")
                .bind("gmId", gmId)
                .map(TemplateMapper())
                .list()
        }

    fun findById(id: UUID): Template? =
        jdbi.withHandle<Template?, Exception> { handle ->
            handle.createQuery("SELECT id, title, description, json_schema, type, gm_id, genre_id, created_at FROM template WHERE id = :id")
                .bind("id", id)
                .map(TemplateMapper())
                .findOne()
                .orElse(null)
        }

    fun findByIdForGm(id: UUID, gmId: UUID): Template? =
        jdbi.withHandle<Template?, Exception> { handle ->
            handle.createQuery("SELECT id, title, description, json_schema, type, gm_id, genre_id, created_at FROM template WHERE id = :id AND gm_id = :gmId")
                .bind("id", id)
                .bind("gmId", gmId)
                .map(TemplateMapper())
                .findOne()
                .orElse(null)
        }

    fun listByGenre(genre: String): List<Template> =
        jdbi.withHandle<List<Template>, Exception> { handle ->
            handle.createQuery("SELECT id, title, description, json_schema, type, gm_id, genre_id, created_at FROM template WHERE genre_id = :genreId")
                .bind("genreId", UUID.fromString(genre))
                .map(TemplateMapper())
                .list()
        }

    fun listByType(type: String): List<Template> =
        jdbi.withHandle<List<Template>, Exception> { handle ->
            handle.createQuery("SELECT id, title, description, json_schema, type, gm_id, genre_id, created_at FROM template WHERE type = :type")
                .bind("type", type)
                .map(TemplateMapper())
                .list()
        }



    fun persist(template: Template) {
        require(!template.type.isNullOrBlank()) { "Template.type must not be null or blank" }
        jdbi.useHandle<Exception> { handle ->
            if (template.createdAt != null) {
                handle.createUpdate("INSERT INTO template (id, title, description, json_schema, type, gm_id, genre_id, created_at) VALUES (:id, :title, :description, :jsonSchema::jsonb, :type, :gmId, :genreId, :createdAt)")
                    .bind("id", template.id)
                    .bind("title", template.title)
                    .bind("description", template.description)
                    .bind("jsonSchema", template.jsonSchema)
                    .bind("type", template.type)
                    .bind("gmId", template.gm?.id)
                    .bind("genreId", template.genre?.id)
                    .bind("createdAt", template.createdAt)
                    .execute()
            } else {
                handle.createUpdate("INSERT INTO template (id, title, description, json_schema, type, gm_id, genre_id) VALUES (:id, :title, :description, :jsonSchema::jsonb, :type, :gmId, :genreId)")
                    .bind("id", template.id)
                    .bind("title", template.title)
                    .bind("description", template.description)
                    .bind("jsonSchema", template.jsonSchema)
                    .bind("type", template.type)
                    .bind("gmId", template.gm?.id)
                    .bind("genreId", template.genre?.id)
                    .execute()
            }
        }
    }

    private class TemplateMapper : RowMapper<Template> {
        override fun map(rs: ResultSet, ctx: StatementContext): Template = Template().apply {
            id = rs.getObject("id", UUID::class.java)
            title = rs.getString("title")
            description = rs.getString("description")
            jsonSchema = rs.getString("json_schema")
            type = rs.getString("type")
            createdAt = rs.getTimestamp("created_at").toInstant()
            gm = GM().apply { id = rs.getObject("gm_id", UUID::class.java) }
            genre = Genre().apply { id = rs.getObject("genre_id", UUID::class.java) }
        }
    }
}
