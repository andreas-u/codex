package org.fg.ttrpg

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
import org.fg.ttrpg.common.dto.CalendarDTO
import org.fg.ttrpg.common.dto.TimelineEventDTO
import org.fg.ttrpg.setting.Setting
import org.fg.ttrpg.setting.SettingRepository
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

@QuarkusTest
class CalendarResourceIT {
    @Inject
    lateinit var gmRepo: GMRepository

    @Inject
    lateinit var settingRepo: SettingRepository

    val gmId = UUID.fromString("00000000-0000-0000-0000-000000000010")

    lateinit var setting: Setting

    @BeforeEach
    fun setup() {
        createGm(gmId)
        setting = createSetting(UUID.randomUUID(), gmId)
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
    fun createSetting(id: UUID, gmId: UUID): Setting {
        val gm = gmRepo.findById(gmId)
        val setting = Setting().apply {
            this.id = id
            title = "world"
            this.gm = gm
        }
        settingRepo.persist(setting)
        return setting
    }

    @Test
    @TestSecurity(user = "userJwt", roles = ["viewer"])
    @JwtSecurity(
        claims = [
            Claim(key = "email", value = "user@gmail.com"),
            Claim(key = "sub", value = "userJwt"),
            Claim(key = "gmId", value = "00000000-0000-0000-0000-000000000010")
        ]
    )
    fun createCalendar_and_event() {
        val calendarDto = CalendarDTO(null, "Cal", "CE", "[]", null, setting.id!!)
        val calendarId =
            given()
                .contentType(ContentType.JSON)
                .body(calendarDto)
                .`when`().post("/api/settings/${setting.id}/calendars")
                .then().statusCode(200)
                .extract().path<String>("id")

        given()
            .`when`().get("/api/calendars/$calendarId")
            .then().statusCode(200)
            .body("name", equalTo("Cal"))

        val eventDto = TimelineEventDTO(null, UUID.fromString(calendarId), "Start", null, 1, null)
        given()
            .contentType(ContentType.JSON)
            .body(eventDto)
            .`when`().post("/api/calendars/$calendarId/events")
            .then().statusCode(200)
            .body("title", equalTo("Start"))
    }
}
