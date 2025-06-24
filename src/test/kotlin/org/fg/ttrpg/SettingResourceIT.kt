package org.fg.ttrpg

import io.quarkus.test.TestTransaction
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.quarkus.test.security.jwt.Claim
import io.quarkus.test.security.jwt.JwtSecurity
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.fg.ttrpg.account.GM
import org.fg.ttrpg.account.GMRepository
import org.fg.ttrpg.common.dto.SettingDTO
import org.fg.ttrpg.common.dto.SettingObjectDTO
import org.fg.ttrpg.setting.*
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.*


@QuarkusTest
class SettingResourceIT {
    @Inject
    lateinit var gmRepo: GMRepository

    @Inject
    lateinit var settingRepo: SettingRepository

    @Inject
    lateinit var objectRepo: SettingObjectRepository

    @Inject
    lateinit var templateRepo: TemplateRepository

    @Inject
    lateinit var genreRepo: org.fg.ttrpg.genre.GenreRepository

    val testGmId1 = UUID.fromString("00000000-0000-0000-0000-000000000001")
    val testGmId2 = UUID.fromString("00000000-0000-0000-0000-000000000002")

    @BeforeEach
    fun setupGm() {
        createGm(testGmId1)
        createGm(testGmId2)
    }

    @AfterEach
    fun cleanup() {
        gmRepo.deleteById(testGmId1)
        gmRepo.deleteById(testGmId2)
    }

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
    fun createGenre(id: UUID, setting: Setting): org.fg.ttrpg.genre.Genre {
        val genre = org.fg.ttrpg.genre.Genre().apply {
            this.id = id
            title = "genre"
            code = "gen-${id.toString().substring(0, 8)}"
            this.setting = setting
        }
        genreRepo.insert(genre)
        return genre
    }

    @Transactional
    fun createTemplate(id: UUID, gmId: UUID, schema: String): Template {
        val gm = gmRepo.findById(gmId)
        val setting = settingRepo.listByGm(gmId).firstOrNull() ?: createSetting(UUID.randomUUID(), gmId)
        val genre = createGenre(UUID.randomUUID(), setting)
        val template = Template().apply {
            this.id = id
            title = "tpl"
            jsonSchema = schema
            this.gm = gm
            this.type = "test"
            this.genre = genre
        }
        templateRepo.persist(template)
        return template
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
}
