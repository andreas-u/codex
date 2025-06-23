package org.fg.ttrpg.setting

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import java.util.UUID

@ApplicationScoped
class SettingObjectRepository @Inject constructor(private val dsl: DSLContext) {
    private val SETTING_OBJECT = DSL.table("setting_object")
    private val SO_ID = DSL.field("id", java.util.UUID::class.java)
    private val SO_SLUG = DSL.field("slug", String::class.java)
    private val SO_NAME = DSL.field("name", String::class.java)
    private val SO_DESCRIPTION = DSL.field("description", String::class.java)
    private val SO_PAYLOAD = DSL.field("payload", String::class.java)
    private val SO_SETTING_ID = DSL.field("setting_id", java.util.UUID::class.java)
    private val SO_TEMPLATE_ID = DSL.field("template_id", java.util.UUID::class.java)
    private val SO_GM_ID = DSL.field("gm_id", java.util.UUID::class.java)
    private val SO_CREATED_AT = DSL.field("created_at", java.time.Instant::class.java)

    fun listBySettingAndGm(settingId: UUID, gmId: UUID): List<SettingObject> =
        dsl.selectFrom(SETTING_OBJECT)
            .where(
                SO_SETTING_ID.eq(settingId)
                    .and(SO_GM_ID.eq(gmId))
            )
            .fetch(::toObject)

    fun listByGm(gmId: UUID): List<SettingObject> =
        dsl.selectFrom(SETTING_OBJECT)
            .where(SO_GM_ID.eq(gmId))
            .fetch(::toObject)

    fun findById(id: UUID): SettingObject? =
        dsl.selectFrom(SETTING_OBJECT)
            .where(SO_ID.eq(id))
            .fetchOne(::toObject)

    fun findByIdForGm(id: UUID, gmId: UUID): SettingObject? =
        dsl.selectFrom(SETTING_OBJECT)
            .where(SO_ID.eq(id).and(SO_GM_ID.eq(gmId)))
            .fetchOne(::toObject)

    fun persist(obj: SettingObject) {
        dsl.insertInto(SETTING_OBJECT)
            .set(SO_ID, obj.id)
            .set(SO_SLUG, obj.slug)
            .set(SO_NAME, obj.title)
            .set(SO_DESCRIPTION, obj.description)
            .set(SO_PAYLOAD, obj.payload)
            .set(SO_SETTING_ID, obj.setting?.id)
            .set(SO_TEMPLATE_ID, obj.template?.id)
            .set(SO_GM_ID, obj.gm?.id)
            .set(SO_CREATED_AT, obj.createdAt)
            .execute()
        // tags are ignored for brevity
    }

    private fun toObject(record: Record): SettingObject = SettingObject().apply {
        id = record.get(SO_ID)
        slug = record.get(SO_SLUG)
        title = record.get(SO_NAME)
        description = record.get(SO_DESCRIPTION)
        payload = record.get(SO_PAYLOAD)
        createdAt = record.get(SO_CREATED_AT)
        setting = Setting().apply { id = record.get(SO_SETTING_ID) }
        template = record.get(SO_TEMPLATE_ID)?.let { Template().apply { id = it } }
        gm = org.fg.ttrpg.account.GM().apply { id = record.get(SO_GM_ID) }
    }
}
