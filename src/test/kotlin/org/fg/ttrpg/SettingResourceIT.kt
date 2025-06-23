package org.fg.ttrpg

import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.fg.ttrpg.account.GM
import org.fg.ttrpg.account.GMRepository
import org.fg.ttrpg.common.dto.SettingDTO
import org.fg.ttrpg.common.dto.SettingObjectDTO
import org.fg.ttrpg.setting.Setting
import org.fg.ttrpg.setting.SettingObjectRepository
import org.fg.ttrpg.setting.SettingRepository
import org.fg.ttrpg.setting.Template
import org.fg.ttrpg.setting.TemplateRepository
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test
import io.smallrye.jwt.build.Jwt
import io.quarkus.test.TestTransaction
import java.util.UUID

@QuarkusTest
@QuarkusTestResource(PostgresTestResource::class)
class SettingResourceIT {
    @Inject
    lateinit var gmRepo: GMRepository

    @Inject
    lateinit var settingRepo: SettingRepository

    @Inject
    lateinit var objectRepo: SettingObjectRepository

    @Inject
    lateinit var templateRepo: TemplateRepository

    private fun token(gmId: UUID) = Jwt.claim("gmId", gmId.toString()).sign()


    @Transactional
    fun createGm(id: UUID) {
        val gm = GM().apply {
            this.id = id
            username = "gm-$id"
        }
        gmRepo.persist(gm)
    }

    @Transactional
    fun createSetting(id: UUID, gmId: UUID): Setting {
        val gm = gmRepo.findById(gmId)
        val setting = Setting().apply {
            this.id = id
            title = "world"
            gm?.let { this.gm = it }
        }
        settingRepo.persist(setting)
        return setting
    }

    @Transactional
    fun createTemplate(id: UUID, gmId: UUID, schema: String): Template {
        val gm = gmRepo.findById(gmId)
        val template = Template().apply {
            this.id = id
            title = "tpl"
            jsonSchema = schema
            this.gm = gm
        }
        templateRepo.persist(template)
        return template
    }

    @Test
    fun createSetting_success() {
        val gmId = UUID.randomUUID()
        createGm(gmId)
        
        given()
            .contentType(ContentType.JSON)
            .body(SettingDTO(null, "World", null, gmId))
            .auth().oauth2(token(gmId))
            .`when`().post("/api/settings")
            .then().statusCode(200)
            .body("title", equalTo("World"))

        verifySettings(gmId, 1)
    }

    @Test
    fun createObject_validationFailure() {
        val gmId = UUID.randomUUID()
        createGm(gmId)
        val settingId = UUID.randomUUID()
        createSetting(settingId, gmId)
        val templateId = UUID.randomUUID()
        createTemplate(templateId, gmId, """{"type":"object","required":["name"]}""")

        given()
            .contentType(ContentType.JSON)
            .body(
                SettingObjectDTO(
                    null,
                    "slug",
                    "Title",
                    payload = "{}",
                    tags = emptyList(),
                    settingId = settingId,
                    templateId = templateId
                )
            )
            .auth().oauth2(token(gmId))
            .`when`().post("/api/settings/$settingId/objects")
            .then().statusCode(422)
    }

    @Test
    fun tenantIsolation() {
        val gm1 = UUID.randomUUID()
        val gm2 = UUID.randomUUID()
        createGm(gm1)
        createGm(gm2)
        val settingId = UUID.randomUUID()
        createSetting(settingId, gm1)

        given()
            .contentType(ContentType.JSON)
            .body(
                SettingObjectDTO(
                    null,
                    "slug",
                    "Title",
                    payload = null,
                    tags = emptyList(),
                    settingId = settingId,
                    templateId = null
                )
            )
            .auth().oauth2(token(gm2))
            .`when`().post("/api/settings/$settingId/objects")
            .then().statusCode(404)
    }

    @TestTransaction
    fun verifySettings(gmId: UUID, expected: Int) {
        val count = settingRepo.list("gm.id", gmId).size
        org.junit.jupiter.api.Assertions.assertEquals(expected, count)
    }
}
