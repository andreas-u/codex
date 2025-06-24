package org.fg.ttrpg

import io.quarkus.test.TestTransaction
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.quarkus.test.security.jwt.Claim
import io.quarkus.test.security.jwt.JwtSecurity
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import jakarta.inject.Inject
import org.fg.ttrpg.common.dto.SettingDTO
import org.fg.ttrpg.common.dto.SettingObjectDTO
import org.fg.ttrpg.setting.*
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.fg.ttrpg.testutils.IntegrationTestHelper
import java.util.*


@QuarkusTest
class SettingResourceIT : IntegrationTestHelper() {
    @Inject
    lateinit var objectRepo: SettingObjectRepository

    val testGmId1 = UUID.fromString("00000000-0000-0000-0000-000000000001")
    val testGmId2 = UUID.fromString("00000000-0000-0000-0000-000000000002")

    @BeforeEach
    fun setupGm() {
        createGm(testGmId1)
        createGm(testGmId2)
    }


    @Test
    @TestSecurity(user = "userJwt", roles = ["viewer"])
    @JwtSecurity(
        claims = [
            Claim(key = "email", value = "user@gmail.com"),
            Claim(key = "sub", value = "userJwt"),
            Claim(key = "gmId", value = "00000000-0000-0000-0000-000000000001")
        ]
    )
    fun createSetting_success() {
        val gmId = testGmId1
        given()
            .contentType(ContentType.JSON)
            .body(SettingDTO(null, "World", null, gmId))
            .`when`().post("/api/settings")
            .then().statusCode(200)
            .body("title", equalTo("World"))

        verifySettings(gmId, 1)
    }

    @Test
    @TestSecurity(user = "userJwt", roles = ["viewer"])
    @JwtSecurity(
        claims = [
            Claim(key = "email", value = "user@gmail.com"),
            Claim(key = "sub", value = "userJwt"),
            Claim(key = "gmId", value = "00000000-0000-0000-0000-000000000001")
        ]
    )
    fun find_setting_by_id() {
        val gmId = testGmId1
        val settingId = UUID.randomUUID()
        createSetting(settingId, gmId)
        given()
            .`when`().get("/api/settings/$settingId")
            .then().statusCode(200)
            .body("title", equalTo("world"))

        verifySettings(gmId, 1)
    }

    @Test
    @TestSecurity(user = "userJwt", roles = ["viewer"])
    @JwtSecurity(
        claims = [
            Claim(key = "email", value = "user@gmail.com"),
            Claim(key = "sub", value = "userJwt"),
            Claim(key = "gmId", value = "00000000-0000-0000-0000-000000000001")
        ]
    )
    fun createObject_validationFailure() {
        val gmId = testGmId1
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
            .`when`().post("/api/settings/$settingId/objects")
            .then().statusCode(422)
    }

    @Test
    @TestSecurity(user = "userJwt", roles = ["viewer"])
    @JwtSecurity(
        claims = [
            Claim(key = "email", value = "user@gmail.com"),
            Claim(key = "sub", value = "userJwt"),
            Claim(key = "gmId", value = "00000000-0000-0000-0000-000000000001")
        ]
    )
    fun createObject_success() {
        val gmId = testGmId1
        val settingId = UUID.randomUUID()
        createSetting(settingId, gmId)
        val templateId = UUID.randomUUID()
        createTemplate(templateId, gmId, "{}")

        given()
            .contentType(ContentType.JSON)
            .body(
                SettingObjectDTO(
                    null,
                    "slug",
                    "Title",
                    payload = "{\"name\":\"obj\"}",
                    tags = emptyList(),
                    settingId = settingId,
                    templateId = templateId
                )
            )
            .`when`().post("/api/settings/$settingId/objects")
            .then().statusCode(200)
            .body("slug", equalTo("slug"))

        verifyObject(settingId, gmId, templateId)
    }

    @Test
    @TestSecurity(user = "userJwt", roles = ["viewer"])
    @JwtSecurity(
        claims = [
            Claim(key = "email", value = "user@gmail.com"),
            Claim(key = "sub", value = "userJwt"),
            Claim(key = "gmId", value = "00000000-0000-0000-0000-000000000002")
        ]
    )
    fun tenantIsolation() {
        val gm1 = testGmId1
        val gm2 = testGmId2
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
            .`when`().post("/api/settings/$settingId/objects")
            .then().statusCode(404)
    }

    @TestTransaction
    fun verifySettings(gmId: UUID, expected: Int) {
        val count = settingRepo.listByGm(gmId).size
        org.junit.jupiter.api.Assertions.assertEquals(expected, count)
    }

    @TestTransaction
    fun verifyObject(settingId: UUID, gmId: UUID, templateId: UUID) {
        val objs = objectRepo.listBySettingAndGm(settingId, gmId)
        org.junit.jupiter.api.Assertions.assertEquals(1, objs.size)
        val obj = objs.first()
        org.junit.jupiter.api.Assertions.assertEquals("slug", obj.slug)
        org.junit.jupiter.api.Assertions.assertEquals("Title", obj.title)
        org.junit.jupiter.api.Assertions.assertEquals("{\"name\": \"obj\"}", obj.payload)
        org.junit.jupiter.api.Assertions.assertEquals(templateId, obj.template?.id)
    }
}
