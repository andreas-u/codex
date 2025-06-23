package org.fg.ttrpg.setting

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.generated.tables.references.TEMPLATE
import java.util.UUID

@ApplicationScoped
class TemplateRepository @Inject constructor(private val dsl: DSLContext) {

    fun listByGm(gmId: UUID): List<Template> =
        dsl.selectFrom(TEMPLATE)
            .where(TEMPLATE.GM_ID.eq(gmId))
            .fetch(::toTemplate)

    fun findById(id: UUID): Template? =
        dsl.selectFrom(TEMPLATE)
            .where(TEMPLATE.ID.eq(id))
            .fetchOne(::toTemplate)

    fun findByIdForGm(id: UUID, gmId: UUID): Template? =
        dsl.selectFrom(TEMPLATE)
            .where(TEMPLATE.ID.eq(id).and(TEMPLATE.GM_ID.eq(gmId)))
            .fetchOne(::toTemplate)

    fun listBySettingAndGm(settingId: UUID, gmId: UUID): List<Template> =
        dsl.selectFrom(TEMPLATE)
            .where(TEMPLATE.GENRE_ID.eq(settingId).and(TEMPLATE.GM_ID.eq(gmId)))
            .fetch(::toTemplate)

    fun listByGenre(genre: String): List<Template> =
        dsl.selectFrom(TEMPLATE)
            .where(TEMPLATE.GENRE_ID.eq(UUID.fromString(genre)))
            .fetch(::toTemplate)

    fun listByType(type: String): List<Template> =
        dsl.selectFrom(TEMPLATE)
            .where(TEMPLATE.TYPE.eq(type))
            .fetch(::toTemplate)

    fun listByGenreAndType(genreId: UUID, type: String): List<Template> =
        dsl.selectFrom(TEMPLATE)
            .where(TEMPLATE.GENRE_ID.eq(genreId).and(TEMPLATE.TYPE.eq(type)))
            .fetch(::toTemplate)

    fun persist(template: Template) {
        dsl.insertInto(TEMPLATE)
            .set(TEMPLATE.ID, template.id)
            .set(TEMPLATE.NAME, template.title)
            .set(TEMPLATE.DESCRIPTION, template.description)
            .set(TEMPLATE.JSON_SCHEMA, template.jsonSchema)
            .set(TEMPLATE.TYPE, template.type)
            .set(TEMPLATE.GM_ID, template.gm?.id)
            .set(TEMPLATE.GENRE_ID, template.genre?.id)
            .set(TEMPLATE.CREATED_AT, template.createdAt)
            .execute()
    }

    private fun toTemplate(record: Record): Template = Template().apply {
        id = record.get(TEMPLATE.ID)
        title = record.get(TEMPLATE.NAME)
        description = record.get(TEMPLATE.DESCRIPTION)
        createdAt = record.get(TEMPLATE.CREATED_AT)
        type = record.get(TEMPLATE.TYPE)
        jsonSchema = record.get(TEMPLATE.JSON_SCHEMA)
        gm = org.fg.ttrpg.account.GM().apply { id = record.get(TEMPLATE.GM_ID) }
        genre = org.fg.ttrpg.genre.Genre().apply { id = record.get(TEMPLATE.GENRE_ID) }
    }
}
