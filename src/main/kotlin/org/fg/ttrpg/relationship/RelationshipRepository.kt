package org.fg.ttrpg.relationship

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import java.util.UUID

@ApplicationScoped
class RelationshipRepository @Inject constructor(private val dsl: DSLContext) {
    private val RELATIONSHIP = DSL.table("relationship")
    private val REL_ID = DSL.field("id", java.util.UUID::class.java)
    private val REL_SETTING_ID = DSL.field("setting_id", java.util.UUID::class.java)
    private val REL_TYPE_ID = DSL.field("type_id", java.util.UUID::class.java)
    private val REL_SOURCE_OBJECT = DSL.field("source_object", java.util.UUID::class.java)
    private val REL_TARGET_OBJECT = DSL.field("target_object", java.util.UUID::class.java)
    private val REL_IS_BIDIRECTIONAL = DSL.field("is_bidirectional", Boolean::class.java)
    private val REL_PROPERTIES = DSL.field("properties", String::class.java)
    private val REL_CREATED_AT = DSL.field("created_at", java.time.Instant::class.java)

    fun listBySetting(settingId: UUID): List<Relationship> =
        dsl.selectFrom(RELATIONSHIP)
            .where(REL_SETTING_ID.eq(settingId))
            .fetch(::toRelationship)

    private fun toRelationship(record: Record): Relationship = Relationship().apply {
        id = record.get(REL_ID)
        setting = org.fg.ttrpg.setting.Setting().apply { id = record.get(REL_SETTING_ID) }
        type = org.fg.ttrpg.relationship.RelationshipType().apply { id = record.get(REL_TYPE_ID) }
        sourceObject = org.fg.ttrpg.setting.SettingObject().apply { id = record.get(REL_SOURCE_OBJECT) }
        targetObject = org.fg.ttrpg.setting.SettingObject().apply { id = record.get(REL_TARGET_OBJECT) }
        isBidirectional = record.get(REL_IS_BIDIRECTIONAL)
        properties = record.get(REL_PROPERTIES)
        createdAt = record.get(REL_CREATED_AT)
    }
}
