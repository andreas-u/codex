package org.fg.ttrpg

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.quarkus.test.security.jwt.Claim
import io.quarkus.test.security.jwt.JwtSecurity
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.fg.ttrpg.campaign.Campaign
import org.fg.ttrpg.common.dto.CampaignEventOverrideDTO
import org.fg.ttrpg.common.dto.TimelineEventDTO
import org.fg.ttrpg.setting.Setting
import org.fg.ttrpg.testutils.IntegrationTestHelper
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

@QuarkusTest
class CampaignEventResourceIT : IntegrationTestHelper() {

    val gmId = UUID.fromString("00000000-0000-0000-0000-000000000020")
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
            Claim(key = "gmId", value = "00000000-0000-0000-0000-000000000020")
        ]
    )
    fun timeline_range_filters_events() {
        val calId = createCalendar(setting.id!!)
        createEvent(calId, "E1", 1)
        createEvent(calId, "E2", 10)
        createEvent(calId, "E3", 20)
        val camp = createCampaign(UUID.randomUUID(), gmId, setting)

        given()
            .`when`().get("/api/campaigns/${camp.id}/timeline?from=5&to=15")
            .then().statusCode(200)
            .body("size()", equalTo(1))
            .body("[0].title", equalTo("E2"))
    }

    @Test
    @TestSecurity(user = "userJwt", roles = ["viewer"])
    @JwtSecurity(
        claims = [
            Claim(key = "email", value = "user@gmail.com"),
            Claim(key = "sub", value = "userJwt"),
            Claim(key = "gmId", value = "00000000-0000-0000-0000-000000000020")
        ]
    )
    fun override_event_patch() {
        val calId = createCalendar(setting.id!!)
        val eventId = createEvent(calId, "Old", 5)
        val camp = createCampaign(UUID.randomUUID(), gmId, setting)

        given()
            .contentType(ContentType.JSON)
            .body(
                CampaignEventOverrideDTO(
                    null,
                    camp.id!!,
                    UUID.fromString(eventId),
                    "PATCH",
                    "{\"title\":\"New\"}"
                )
            )
            .`when`().patch("/api/campaigns/${camp.id}/events/$eventId")
            .then().statusCode(200)
            .body("title", equalTo("New"))

        given()
            .`when`().get("/api/campaigns/${camp.id}/timeline")
            .then().statusCode(200)
            .body("size()", equalTo(1))
            .body("[0].title", equalTo("New"))
    }

    @Test
    @TestSecurity(user = "userJwt", roles = ["viewer"])
    @JwtSecurity(
        claims = [
            Claim(key = "email", value = "user@gmail.com"),
            Claim(key = "sub", value = "userJwt"),
            Claim(key = "gmId", value = "00000000-0000-0000-0000-000000000020")
        ]
    )
    fun override_event_delete() {
        val calId = createCalendar(setting.id!!)
        val eventId = createEvent(calId, "Gone", 3)
        val camp = createCampaign(UUID.randomUUID(), gmId, setting)

        given()
            .contentType(ContentType.JSON)
            .body(
                CampaignEventOverrideDTO(
                    null,
                    camp.id!!,
                    UUID.fromString(eventId),
                    "DELETE",
                    null
                )
            )
            .`when`().patch("/api/campaigns/${camp.id}/events/$eventId")
            .then().log().body().statusCode(204)

        given()
            .`when`().get("/api/campaigns/${camp.id}/timeline")
            .then().statusCode(200)
            .log().body() // Log the response body for debugging
            .body("size()", equalTo(0))
    }
}
