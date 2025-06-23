package org.fg.ttrpg.setting

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.generated.tables.references.SETTING_OBJECT
import java.util.UUID

@ApplicationScoped
class SettingObjectRepository @Inject constructor(private val dsl: DSLContext) {

    fun listBySettingAndGm(settingId: UUID, gmId: UUID): List<SettingObject> =
        dsl.selectFrom(SETTING_OBJECT)
            .where(
                SETTING_OBJECT.SETTING_ID.eq(settingId)
                    .and(SETTING_OBJECT.GM_ID.eq(gmId))
            )
            .fetch(::toObject)

    fun listByGm(gmId: UUID): List<SettingObject> =
        dsl.selectFrom(SETTING_OBJECT)
            .where(SETTING_OBJECT.GM_ID.eq(gmId))
            .fetch(::toObject)

    fun findById(id: UUID): SettingObject? =
        dsl.selectFrom(SETTING_OBJECT)
            .where(SETTING_OBJECT.ID.eq(id))
            .fetchOne(::toObject)

    fun findByIdForGm(id: UUID, gmId: UUID): SettingObject? =
        dsl.selectFrom(SETTING_OBJECT)
            .where(SETTING_OBJECT.ID.eq(id).and(SETTING_OBJECT.GM_ID.eq(gmId)))
            .fetchOne(::toObject)

    fun persist(obj: SettingObject) {
        dsl.insertInto(SETTING_OBJECT)
            .set(SETTING_OBJECT.ID, obj.id)
            .set(SETTING_OBJECT.SLUG, obj.slug)
            .set(SETTING_OBJECT.NAME, obj.title)
            .set(SETTING_OBJECT.DESCRIPTION, obj.description)
            .set(SETTING_OBJECT.PAYLOAD, obj.payload)
            .set(SETTING_OBJECT.SETTING_ID, obj.setting?.id)
            .set(SETTING_OBJECT.TEMPLATE_ID, obj.template?.id)
            .set(SETTING_OBJECT.GM_ID, obj.gm?.id)
            .set(SETTING_OBJECT.CREATED_AT, obj.createdAt)
            .execute()
        // tags are ignored for brevity
    }

    private fun toObject(record: Record): SettingObject = SettingObject().apply {
        id = record.get(SETTING_OBJECT.ID)
        slug = record.get(SETTING_OBJECT.SLUG)
        title = record.get(SETTING_OBJECT.NAME)
        description = record.get(SETTING_OBJECT.DESCRIPTION)
        payload = record.get(SETTING_OBJECT.PAYLOAD)
        createdAt = record.get(SETTING_OBJECT.CREATED_AT)
        setting = Setting().apply { id = record.get(SETTING_OBJECT.SETTING_ID) }
        template = record.get(SETTING_OBJECT.TEMPLATE_ID)?.let { Template().apply { id = it } }
        gm = org.fg.ttrpg.account.GM().apply { id = record.get(SETTING_OBJECT.GM_ID) }
    }
}
