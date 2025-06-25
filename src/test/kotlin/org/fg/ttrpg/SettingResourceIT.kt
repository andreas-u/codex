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
import io.kotest.matchers.shouldBe
import org.fg.ttrpg.testutils.IntegrationTestHelper
import java.util.*


@QuarkusTest
class SettingResourceIT : IntegrationTestHelper() {
    @Inject
    lateinit var objectRepo: SettingObjectRepository

    val testGmId1 = UUID.fromString("00000000-0000-0000-0000-000000000001")
    val testGmId2 = UUID.fromString("00000000-0000-0000-0000-000000000002")
    lateinit var userId1: UUID
    lateinit var userId2: UUID

    companion object {
        const val USER_ID1 = "d5bbc73c-1e43-5cd6-919c-af383f4fe05a"
        const val USER_ID2 = "750be2fc-07b4-5742-8ef5-722fb199ab95"
    }

    @BeforeEach
    fun setupGm() {
        createGm(testGmId1)
        createGm(testGmId2)
        userId1 = UUID.fromString(USER_ID1)
        userId2 = UUID.fromString(USER_ID2)
    }


    @Test
    @TestSecurity(user = "userJwt", roles = ["viewer"])
    @JwtSecurity(
        claims = [
            Claim(key = "email", value = "user@gmail.com"),
            Claim(key = "sub", value = "userJwt"),
            Claim(key = "userId", value = USER_ID1)
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
            Claim(key = "userId", value = USER_ID1)
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
            Claim(key = "userId", value = USER_ID1)
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
            Claim(key = "userId", value = USER_ID1)
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
            Claim(key = "userId", value = USER_ID2)
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
        count shouldBe expected
    }

    @TestTransaction
    fun verifyObject(settingId: UUID, gmId: UUID, templateId: UUID) {
        val objs = objectRepo.listBySettingAndGm(settingId, gmId)
        objs.size shouldBe 1
        val obj = objs.first()
        obj.slug shouldBe "slug"
        obj.title shouldBe "Title"
        obj.payload shouldBe "{\"name\": \"obj\"}"
        obj.template?.id shouldBe templateId
    }
}
