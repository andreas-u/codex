package org.fg.ttrpg.relationship

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.generated.tables.references.RELATIONSHIP
import java.util.UUID

@ApplicationScoped
class RelationshipRepository @Inject constructor(private val dsl: DSLContext) {

    fun listBySetting(settingId: UUID): List<Relationship> =
        dsl.selectFrom(RELATIONSHIP)
            .where(RELATIONSHIP.SETTING_ID.eq(settingId))
            .fetch(::toRelationship)

    private fun toRelationship(record: Record): Relationship = Relationship().apply {
        id = record.get(RELATIONSHIP.ID)
        setting = org.fg.ttrpg.setting.Setting().apply { id = record.get(RELATIONSHIP.SETTING_ID) }
        type = org.fg.ttrpg.relationship.RelationshipType().apply { id = record.get(RELATIONSHIP.TYPE_ID) }
        sourceObject = org.fg.ttrpg.setting.SettingObject().apply { id = record.get(RELATIONSHIP.SOURCE_OBJECT) }
        targetObject = org.fg.ttrpg.setting.SettingObject().apply { id = record.get(RELATIONSHIP.TARGET_OBJECT) }
        isBidirectional = record.get(RELATIONSHIP.IS_BIDIRECTIONAL)
        properties = record.get(RELATIONSHIP.PROPERTIES)
        createdAt = record.get(RELATIONSHIP.CREATED_AT)
    }
}
