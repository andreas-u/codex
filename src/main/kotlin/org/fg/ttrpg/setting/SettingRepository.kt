package org.fg.ttrpg.setting

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import java.util.UUID

@ApplicationScoped
class SettingRepository @Inject constructor(private val dsl: DSLContext) {
    private val SETTING = DSL.table("setting")
    private val SETTING_ID = DSL.field("id", java.util.UUID::class.java)
    private val SETTING_NAME = DSL.field("name", String::class.java)
    private val SETTING_DESCRIPTION = DSL.field("description", String::class.java)
    private val SETTING_GM_ID = DSL.field("gm_id", java.util.UUID::class.java)
    private val SETTING_CREATED_AT = DSL.field("created_at", java.time.Instant::class.java)

    fun listByGm(gmId: UUID): List<Setting> =
        dsl.selectFrom(SETTING)
            .where(SETTING_GM_ID.eq(gmId))
            .fetch(::toSetting)

    fun findById(id: UUID): Setting? =
        dsl.selectFrom(SETTING)
            .where(SETTING_ID.eq(id))
            .fetchOne(::toSetting)

    fun findByIdForGm(id: UUID, gmId: UUID): Setting? =
        dsl.selectFrom(SETTING)
            .where(SETTING_ID.eq(id).and(SETTING_GM_ID.eq(gmId)))
            .fetchOne(::toSetting)

    fun persist(setting: Setting) {
        dsl.insertInto(SETTING)
            .set(SETTING_ID, setting.id)
            .set(SETTING_NAME, setting.title)
            .set(SETTING_DESCRIPTION, setting.description)
            .set(SETTING_GM_ID, setting.gm?.id)
            .set(SETTING_CREATED_AT, setting.createdAt)
            .execute()
    }

    private fun toSetting(record: Record): Setting = Setting().apply {
        id = record.get(SETTING_ID)
        title = record.get(SETTING_NAME)
        description = record.get(SETTING_DESCRIPTION)
        createdAt = record.get(SETTING_CREATED_AT)
        gm = org.fg.ttrpg.account.GM().apply { id = record.get(SETTING_GM_ID) }
    }
}
