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
import org.fg.ttrpg.campaign.Campaign
import org.fg.ttrpg.campaign.CampaignRepository
import org.fg.ttrpg.common.dto.CalendarDTO
import org.fg.ttrpg.common.dto.CampaignEventOverrideDTO
import org.fg.ttrpg.common.dto.TimelineEventDTO
import org.fg.ttrpg.setting.Setting
import org.fg.ttrpg.setting.SettingRepository
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

@QuarkusTest
class CampaignEventResourceIT {
    @Inject
    lateinit var gmRepo: GMRepository

    @Inject
    lateinit var settingRepo: SettingRepository

    @Inject
    lateinit var campaignRepo: CampaignRepository

    val gmId = UUID.fromString("00000000-0000-0000-0000-000000000020")
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

    @Transactional
    fun createCampaign(id: UUID, gmId: UUID, setting: Setting): Campaign {
        val gm = gmRepo.findById(gmId)
        val camp = Campaign().apply {
            this.id = id
            title = "camp"
            startedOn = Instant.now()
            this.gm = gm
            this.setting = setting
        }
        campaignRepo.persist(camp)
        return camp
    }

    fun createCalendar(settingId: UUID): String {
        val dto = CalendarDTO(null, "Cal", "CE", "[]", null, settingId)
        return given()
            .contentType(ContentType.JSON)
            .body(dto)
            .`when`().post("/api/settings/$settingId/calendars")
            .then().statusCode(200)
            .extract().path("id")
    }

    fun createEvent(calId: String, title: String, day: Int): String {
        val dto = TimelineEventDTO(null, UUID.fromString(calId), title, null, day, null)
        return given()
            .contentType(ContentType.JSON)
            .body(dto)
            .`when`().post("/api/calendars/$calId/events")
            .then().statusCode(200)
            .extract().path("id")
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
            .then().statusCode(404)

        given()
            .`when`().get("/api/campaigns/${camp.id}/timeline")
            .then().statusCode(200)
            .body("size()", equalTo(0))
    }
}

