package org.fg.ttrpg.setting

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.fg.ttrpg.account.GM
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class SettingObjectRepository @Inject constructor(
    private val jdbi: Jdbi,
    private val tagRepo: SettingObjectTagRepository,
) {

    fun listBySettingAndGm(settingId: UUID, gmId: UUID): List<SettingObject> =
        jdbi.withHandle<List<SettingObject>, Exception> { handle ->
            handle.createQuery(
                "SELECT id, slug, title, description, payload, setting_id, template_id, gm_id, created_at FROM setting_object WHERE setting_id = :settingId AND gm_id = :gmId"
            )
                .bind("settingId", settingId)
                .bind("gmId", gmId)
                .map(SettingObjectMapper())
                .list()
        }.onEach { obj ->
            obj.id?.let { obj.tags = tagRepo.listBySettingObject(it).toMutableList() }
        }

    fun listByGm(gmId: UUID): List<SettingObject> =
        jdbi.withHandle<List<SettingObject>, Exception> { handle ->
            handle.createQuery(
                "SELECT id, slug, title, description, payload, setting_id, template_id, gm_id, created_at FROM setting_object WHERE gm_id = :gmId"
            )
                .bind("gmId", gmId)
                .map(SettingObjectMapper())
                .list()
        }.onEach { obj ->
            obj.id?.let { obj.tags = tagRepo.listBySettingObject(it).toMutableList() }
        }

    fun findById(id: UUID): SettingObject? =
        jdbi.withHandle<SettingObject?, Exception> { handle ->
            handle.createQuery(
                "SELECT id, slug, title, description, payload, setting_id, template_id, gm_id, created_at FROM setting_object WHERE id = :id"
            )
                .bind("id", id)
                .map(SettingObjectMapper())
                .findOne()
                .orElse(null)
        }?.also { obj ->
            obj.id?.let { obj.tags = tagRepo.listBySettingObject(it).toMutableList() }
        }

    fun findByIdForGm(id: UUID, gmId: UUID): SettingObject? =
        jdbi.withHandle<SettingObject?, Exception> { handle ->
            handle.createQuery(
                "SELECT id, slug, title, description, payload, setting_id, template_id, gm_id, created_at FROM setting_object WHERE id = :id AND gm_id = :gmId"
            )
                .bind("id", id)
                .bind("gmId", gmId)
                .map(SettingObjectMapper())
                .findOne()
                .orElse(null)
        }?.also { obj ->
            obj.id?.let { obj.tags = tagRepo.listBySettingObject(it).toMutableList() }
        }

    fun persist(obj: SettingObject) {
        if (obj.id == null) {
            obj.id = java.util.UUID.randomUUID()
        }
        if (obj.createdAt == null) {
            obj.createdAt = java.time.Instant.now()
        }
        jdbi.useHandle<Exception> { handle ->
            handle.createUpdate(
                "INSERT INTO setting_object (id, slug, title, description, payload, setting_id, template_id, gm_id, created_at) VALUES (:id, :slug, :title, :description, :payload::jsonb, :settingId, :templateId, :gmId, :createdAt)"
            )
                .bind("id", obj.id)
                .bind("slug", obj.slug)
                .bind("title", obj.title)
                .bind("description", obj.description)
                .bind("payload", obj.payload)
                .bind("settingId", obj.setting?.id)
                .bind("templateId", obj.template?.id)
                .bind("gmId", obj.gm?.id)
                .bind("createdAt", obj.createdAt)
                .execute()
        }
        obj.id?.let { tagRepo.replaceForObject(it, obj.tags) }
    }

    private class SettingObjectMapper : RowMapper<SettingObject> {
        override fun map(rs: ResultSet, ctx: StatementContext): SettingObject = SettingObject().apply {
            id = rs.getObject("id", UUID::class.java)
            slug = rs.getString("slug")
            title = rs.getString("title")
            description = rs.getString("description")
            payload = rs.getString("payload")
            createdAt = rs.getTimestamp("created_at").toInstant()
            setting = Setting().apply { id = rs.getObject("setting_id", UUID::class.java) }
            val templateId = rs.getObject("template_id", UUID::class.java)
            if (templateId != null) {
                template = Template().apply { id = templateId }
            }
            gm = GM().apply { id = rs.getObject("gm_id", UUID::class.java) }
        }
    }
}
