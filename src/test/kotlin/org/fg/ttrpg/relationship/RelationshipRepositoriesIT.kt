package org.fg.ttrpg.relationship

import io.kotest.matchers.shouldBe
import io.quarkus.test.TestTransaction
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Test
import java.util.UUID

@QuarkusTest
class RelationshipRepositoriesIT {
    @Inject
    lateinit var jdbi: Jdbi

    @Inject
    lateinit var typeRepo: RelationshipTypeRepository

    @Inject
    lateinit var overrideRepo: RelationshipOverrideRepository

    @Inject
    lateinit var relationshipRepo: RelationshipRepository

    @Test
    @TestTransaction
    fun `relationship type CRUD`() {
        val typeId = UUID.randomUUID()
        jdbi.useHandle<Exception> { handle ->
            handle.createUpdate("INSERT INTO relationship_type (id, code, display_name, directional, created_at) VALUES (:id, :code, :dn, false, now())")
                .bind("id", typeId)
                .bind("code", "ally")
                .bind("dn", "Ally")
                .execute()
        }

        val found = typeRepo.findById(typeId)
        found?.code shouldBe "ally"

        found!!.displayName = "Buddy"
        typeRepo.update(found)
        typeRepo.findById(typeId)?.displayName shouldBe "Buddy"

        typeRepo.deleteById(typeId)
        typeRepo.findById(typeId) shouldBe null
    }

    @Test
    @TestTransaction
    fun `relationship override CRUD`() {
        val gmId = UUID.randomUUID()
        val settingId = UUID.randomUUID()
        val campaignId = UUID.randomUUID()
        val overrideId = UUID.randomUUID()

        jdbi.useHandle<Exception> { h ->
            h.execute("INSERT INTO gm (id, username) VALUES (?, ?)", gmId, "gm")
            h.execute("INSERT INTO setting (id, name, gm_id, created_at) VALUES (?, ?, ?, now())", settingId, "world", gmId)
            h.execute("INSERT INTO campaign (id, name, gm_id, setting_id, started_on) VALUES (?, ?, ?, ?, now())", campaignId, "camp", gmId, settingId)
            h.createUpdate("INSERT INTO relationship_override (id, campaign_id, override_mode, properties, created_at) VALUES (:id, :campaignId, :mode, '{}'::jsonb, now())")
                .bind("id", overrideId)
                .bind("campaignId", campaignId)
                .bind("mode", "PATCH")
                .execute()
        }

        val found = overrideRepo.findById(overrideId)
        found?.overrideMode shouldBe OverrideMode.PATCH

        found!!.overrideMode = OverrideMode.REPLACE
        overrideRepo.update(found)
        overrideRepo.findById(overrideId)?.overrideMode shouldBe OverrideMode.REPLACE

        overrideRepo.deleteById(overrideId)
        overrideRepo.findById(overrideId) shouldBe null
    }

    @Test
    @TestTransaction
    fun `list relationships by setting`() {
        val gmId = UUID.randomUUID()
        val settingId = UUID.randomUUID()
        val typeId = UUID.randomUUID()
        val relId = UUID.randomUUID()
        val srcObj = UUID.randomUUID()
        val tgtObj = UUID.randomUUID()

        jdbi.useHandle<Exception> { h ->
            h.execute("INSERT INTO gm (id, username) VALUES (?, ?)", gmId, "gm")
            h.execute("INSERT INTO setting (id, name, gm_id, created_at) VALUES (?, ?, ?, now())", settingId, "world", gmId)
            h.execute("INSERT INTO setting_object (id, slug, name, setting_id, gm_id, created_at) VALUES (?, 'src', 'src', ?, ?, now())", srcObj, settingId, gmId)
            h.execute("INSERT INTO setting_object (id, slug, name, setting_id, gm_id, created_at) VALUES (?, 'tgt', 'tgt', ?, ?, now())", tgtObj, settingId, gmId)
            h.execute("INSERT INTO relationship_type (id, setting_id, code, display_name, directional, created_at) VALUES (?, ?, 'friend', 'Friend', false, now())", typeId, settingId)
            h.execute("INSERT INTO relationship (id, setting_id, type_id, source_object, target_object, is_bidirectional, created_at) VALUES (?, ?, ?, ?, ?, false, now())", relId, settingId, typeId, srcObj, tgtObj)
        }

        val list = relationshipRepo.listBySetting(settingId)
        list.size shouldBe 1
        list.first().id shouldBe relId
    }
}
