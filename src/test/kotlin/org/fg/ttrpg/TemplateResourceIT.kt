package org.fg.ttrpg

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.quarkus.test.security.jwt.Claim
import io.quarkus.test.security.jwt.JwtSecurity
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.fg.ttrpg.account.GM
import org.fg.ttrpg.account.GMRepository
import org.fg.ttrpg.setting.*
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

@QuarkusTest
class TemplateResourceIT {
    @Inject
    lateinit var gmRepo: GMRepository

    @Inject
    lateinit var settingRepo: SettingRepository

    @Inject
    lateinit var templateRepo: TemplateRepository

    @Inject
    lateinit var genreRepo: org.fg.ttrpg.genre.GenreRepository

    val gmId = UUID.fromString("00000000-0000-0000-0000-000000000001")

    @BeforeEach
    fun setup() {
        createGm(gmId)
    }

    @AfterEach
    fun cleanup() {
        gmRepo.deleteById(gmId)
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
    fun createSetting(gmId: UUID): Pair<Setting, org.fg.ttrpg.genre.Genre> {
        val gm = gmRepo.findById(gmId)
        val setting = Setting().apply {
            id = UUID.randomUUID()
            title = "world"
            this.gm = gm
        }
        settingRepo.persist(setting)
        val genre = org.fg.ttrpg.genre.Genre().apply {
            id = UUID.randomUUID()
            title = "genre"
            code = "code-${id.toString().substring(0,8)}"
            this.setting = setting
        }
        genreRepo.insert(genre)
        return setting to genre
    }

    @Transactional
    fun createTemplate(genre: org.fg.ttrpg.genre.Genre, gmId: UUID, type: String): Template {
        val gm = gmRepo.findById(gmId)
        val template = Template().apply {
            id = UUID.randomUUID()
            title = "tpl"
            this.type = type
            jsonSchema = "{}"
            this.genre = genre
            this.gm = gm
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
    fun listBySettingId() {
        val (setting1, genre1) = createSetting(gmId)
        val (setting2, _) = createSetting(gmId)
        createTemplate(genre1, gmId, "npc")
        createTemplate(createSetting(gmId).second, gmId, "item")

        given()
            .`when`().get("/api/templates?settingId=${setting1.id}")
            .then().statusCode(200)
            .body("size()", equalTo(1))
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
    fun listByGenre() {
        val (setting, genre) = createSetting(gmId)
        createTemplate(genre, gmId, "npc")
        createTemplate(createSetting(gmId).second, gmId, "item")

        given()
            .`when`().get("/api/templates?genre=${genre.id}")
            .then().statusCode(200)
            .body("size()", equalTo(1))
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
    fun listByType() {
        val (setting, genre) = createSetting(gmId)
        createTemplate(genre, gmId, "npc")
        createTemplate(createSetting(gmId).second, gmId, "item")

        given()
            .`when`().get("/api/templates?type=npc")
            .then().statusCode(200)
            .body("size()", equalTo(1))
    }
}
