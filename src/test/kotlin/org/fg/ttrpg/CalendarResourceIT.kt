package org.fg.ttrpg

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.quarkus.test.security.jwt.Claim
import io.quarkus.test.security.jwt.JwtSecurity
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.fg.ttrpg.setting.Setting
import org.fg.ttrpg.testutils.IntegrationTestHelper
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

@QuarkusTest
class CalendarResourceIT : IntegrationTestHelper() {

    val gmId = UUID.fromString("00000000-0000-0000-0000-000000000010")

    lateinit var setting: Setting

    @BeforeEach
    fun setup() {
        createGm(gmId)
        setting = createSetting(UUID.randomUUID(), gmId)
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
        val calendarId = createCalendar(setting.id!!)

        given()
            .`when`().get("/api/calendars/$calendarId")
            .then().statusCode(200)
            .body("name", equalTo("Cal"))

        val eventId = createEvent(calendarId, "Start", 1)
        given()
            .`when`().get("/api/calendars/$calendarId/events/$eventId")
            .then().statusCode(200)
            .body("title", equalTo("Start"))
    }
}
