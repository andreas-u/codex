package org.fg.ttrpg.setting

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import java.util.UUID

@ApplicationScoped
class TemplateRepository @Inject constructor(private val dsl: DSLContext) {
    private val TEMPLATE = DSL.table("template")
    private val TEMPLATE_ID = DSL.field("id", java.util.UUID::class.java)
    private val TEMPLATE_NAME = DSL.field("name", String::class.java)
    private val TEMPLATE_DESCRIPTION = DSL.field("description", String::class.java)
    private val TEMPLATE_JSON_SCHEMA = DSL.field("json_schema", String::class.java)
    private val TEMPLATE_TYPE = DSL.field("type", String::class.java)
    private val TEMPLATE_GM_ID = DSL.field("gm_id", java.util.UUID::class.java)
    private val TEMPLATE_GENRE_ID = DSL.field("genre_id", java.util.UUID::class.java)
    private val TEMPLATE_CREATED_AT = DSL.field("created_at", java.time.Instant::class.java)

    fun listByGm(gmId: UUID): List<Template> =
        dsl.selectFrom(TEMPLATE)
            .where(TEMPLATE_GM_ID.eq(gmId))
            .fetch(::toTemplate)

    fun findById(id: UUID): Template? =
        dsl.selectFrom(TEMPLATE)
            .where(TEMPLATE_ID.eq(id))
            .fetchOne(::toTemplate)

    fun findByIdForGm(id: UUID, gmId: UUID): Template? =
        dsl.selectFrom(TEMPLATE)
            .where(TEMPLATE_ID.eq(id).and(TEMPLATE_GM_ID.eq(gmId)))
            .fetchOne(::toTemplate)

    fun listBySettingAndGm(settingId: UUID, gmId: UUID): List<Template> =
        dsl.selectFrom(TEMPLATE)
            .where(TEMPLATE_GENRE_ID.eq(settingId).and(TEMPLATE_GM_ID.eq(gmId)))
            .fetch(::toTemplate)

    fun listByGenre(genre: String): List<Template> =
        dsl.selectFrom(TEMPLATE)
            .where(TEMPLATE_GENRE_ID.eq(UUID.fromString(genre)))
            .fetch(::toTemplate)

    fun listByType(type: String): List<Template> =
        dsl.selectFrom(TEMPLATE)
            .where(TEMPLATE_TYPE.eq(type))
            .fetch(::toTemplate)

    fun listByGenreAndType(genreId: UUID, type: String): List<Template> =
        dsl.selectFrom(TEMPLATE)
            .where(TEMPLATE_GENRE_ID.eq(genreId).and(TEMPLATE_TYPE.eq(type)))
            .fetch(::toTemplate)

    fun persist(template: Template) {
        dsl.insertInto(TEMPLATE)
            .set(TEMPLATE_ID, template.id)
            .set(TEMPLATE_NAME, template.title)
            .set(TEMPLATE_DESCRIPTION, template.description)
            .set(TEMPLATE_JSON_SCHEMA, template.jsonSchema)
            .set(TEMPLATE_TYPE, template.type)
            .set(TEMPLATE_GM_ID, template.gm?.id)
            .set(TEMPLATE_GENRE_ID, template.genre?.id)
            .set(TEMPLATE_CREATED_AT, template.createdAt)
            .execute()
    }

    private fun toTemplate(record: Record): Template = Template().apply {
        id = record.get(TEMPLATE_ID)
        title = record.get(TEMPLATE_NAME)
        description = record.get(TEMPLATE_DESCRIPTION)
        createdAt = record.get(TEMPLATE_CREATED_AT)
        type = record.get(TEMPLATE_TYPE)
        jsonSchema = record.get(TEMPLATE_JSON_SCHEMA)
        gm = org.fg.ttrpg.account.GM().apply { id = record.get(TEMPLATE_GM_ID) }
        genre = org.fg.ttrpg.genre.Genre().apply { id = record.get(TEMPLATE_GENRE_ID) }
    }
}
