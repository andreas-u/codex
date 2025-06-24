package org.fg.ttrpg.testutils

import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.fg.ttrpg.account.GM
import org.fg.ttrpg.account.GMRepository
import org.fg.ttrpg.campaign.Campaign
import org.fg.ttrpg.campaign.CampaignRepository
import org.fg.ttrpg.common.dto.CalendarDTO
import org.fg.ttrpg.common.dto.TimelineEventDTO
import org.fg.ttrpg.setting.*
import org.junit.jupiter.api.AfterEach
import java.time.Instant
import java.util.UUID

open class IntegrationTestHelper {
    @Inject
    lateinit var gmRepo: GMRepository

    @Inject
    lateinit var settingRepo: SettingRepository

    @Inject
    lateinit var templateRepo: TemplateRepository

    @Inject
    lateinit var campaignRepo: CampaignRepository

    @Inject
    lateinit var genreRepo: org.fg.ttrpg.genre.GenreRepository

    private val gmIds = mutableListOf<UUID>()

    @AfterEach
    fun cleanup() {
        gmIds.forEach { gmRepo.deleteById(it) }
        gmIds.clear()
    }

    @Transactional
    open fun createGm(id: UUID): GM {
        val gm = GM().apply {
            this.id = id
            username = "gm-$id"
        }
        gmRepo.persist(gm)
        gmIds.add(id)
        return gm
    }

    @Transactional
    open fun createSetting(id: UUID, gmId: UUID): Setting {
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
    open fun createGenre(id: UUID, setting: Setting): org.fg.ttrpg.genre.Genre {
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
    open fun createTemplate(id: UUID, gmId: UUID, schema: String, type: String = "test"): Template {
        val gm = gmRepo.findById(gmId)
        val setting = settingRepo.listByGm(gmId).firstOrNull() ?: createSetting(UUID.randomUUID(), gmId)
        val genre = createGenre(UUID.randomUUID(), setting)
        val template = Template().apply {
            this.id = id
            title = "tpl"
            jsonSchema = schema
            this.type = type
            this.gm = gm
            this.genre = genre
        }
        templateRepo.persist(template)
        return template
    }

    @Transactional
    open fun createCampaign(id: UUID, gmId: UUID, setting: Setting): Campaign {
        val gm = gmRepo.findById(gmId)
        val campaign = Campaign().apply {
            this.id = id
            title = "camp"
            startedOn = Instant.now()
            this.gm = gm
            this.setting = setting
        }
        campaignRepo.persist(campaign)
        return campaign
    }

    open fun createCalendar(settingId: UUID): String {
        val dto = CalendarDTO(null, "Cal", "CE", "[]", null, settingId)
        return given()
            .contentType(ContentType.JSON)
            .body(dto)
            .`when`().post("/api/settings/$settingId/calendars")
            .then().statusCode(200)
            .extract().path("id")
    }

    open fun createEvent(calendarId: String, title: String, day: Int): String {
        val dto = TimelineEventDTO(null, UUID.fromString(calendarId), title, null, day, null)
        return given()
            .contentType(ContentType.JSON)
            .body(dto)
            .`when`().post("/api/calendars/$calendarId/events")
            .then().statusCode(200)
            .extract().path("id")
    }
}
