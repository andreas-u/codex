package org.fg.ttrpg.setting

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.generated.tables.references.SETTING
import java.util.UUID

@ApplicationScoped
class SettingRepository @Inject constructor(private val dsl: DSLContext) {

    fun listByGm(gmId: UUID): List<Setting> =
        dsl.selectFrom(SETTING)
            .where(SETTING.GM_ID.eq(gmId))
            .fetch(::toSetting)

    fun findById(id: UUID): Setting? =
        dsl.selectFrom(SETTING)
            .where(SETTING.ID.eq(id))
            .fetchOne(::toSetting)

    fun findByIdForGm(id: UUID, gmId: UUID): Setting? =
        dsl.selectFrom(SETTING)
            .where(SETTING.ID.eq(id).and(SETTING.GM_ID.eq(gmId)))
            .fetchOne(::toSetting)

    fun persist(setting: Setting) {
        dsl.insertInto(SETTING)
            .set(SETTING.ID, setting.id)
            .set(SETTING.NAME, setting.title)
            .set(SETTING.DESCRIPTION, setting.description)
            .set(SETTING.GM_ID, setting.gm?.id)
            .set(SETTING.CREATED_AT, setting.createdAt)
            .execute()
    }

    private fun toSetting(record: Record): Setting = Setting().apply {
        id = record.get(SETTING.ID)
        title = record.get(SETTING.NAME)
        description = record.get(SETTING.DESCRIPTION)
        createdAt = record.get(SETTING.CREATED_AT)
        gm = org.fg.ttrpg.account.GM().apply { id = record.get(SETTING.GM_ID) }
    }
}
