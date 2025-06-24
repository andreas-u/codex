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
import org.fg.ttrpg.campaign.CampaignObject
import org.fg.ttrpg.campaign.CampaignObjectRepository
import org.fg.ttrpg.campaign.CampaignRepository
import org.fg.ttrpg.setting.*
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*


@QuarkusTest
class CampaignResourceIT {
    @Inject
    lateinit var gmRepo: GMRepository

    @Inject
    lateinit var settingRepo: SettingRepository

    @Inject
    lateinit var templateRepo: TemplateRepository

    @Inject
    lateinit var settingObjectRepo: SettingObjectRepository

    @Inject
    lateinit var campaignRepo: CampaignRepository

    @Inject
    lateinit var campaignObjectRepo: CampaignObjectRepository

    @Inject
    lateinit var genreRepo: org.fg.ttrpg.genre.GenreRepository


    @Transactional
    fun createGm(id: UUID) {
        val gm = GM().apply {
            this.id = id
            username = "gm-$id"
        }
        gmRepo.persist(gm)
    }

    @Transactional
    fun createGenre(id: UUID, setting: Setting): org.fg.ttrpg.genre.Genre {
        val genre = org.fg.ttrpg.genre.Genre().apply {
            this.id = id
            title = "genre"
            code = "gen-${id.toString().substring(0, 8)}"  // Make code unique by including part of the UUID
            this.setting = setting
        }
        genreRepo.insert(genre)
        return genre
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
        // Create a setting and genre for the template
        val setting = createSetting(UUID.randomUUID(), gmId)
        val genre = createGenre(UUID.randomUUID(), setting)
        val template = Template().apply {
            this.id = id
            title = "tpl"
            type = "test"  // Setting the required type field
            jsonSchema = schema
            this.gm = gm
            this.genre = genre  // Setting the required genre field
        }
        templateRepo.persist(template)
        return template
    }

    @Transactional
    fun createSettingObject(id: UUID, setting: Setting, template: Template, gmId: UUID): SettingObject {
        val gm = gmRepo.findById(gmId)
        val obj = SettingObject().apply {
            this.id = id
            slug = "slug"
            title = "obj"
            this.setting = setting
            this.template = template
            this.gm = gm
            payload = "{}"
            createdAt = java.time.Instant.now()  // Setting the required createdAt field
        }
        settingObjectRepo.persist(obj)
        return obj
    }

    @Transactional
    fun createCampaign(id: UUID, gmId: UUID, setting: Setting): Campaign {
        val gm = gmRepo.findById(gmId)
        val camp = Campaign().apply {
            this.id = id
            title = "camp"
            startedOn = java.time.Instant.now()  // Setting the required startedOn field
            this.gm = gm
            this.setting = setting
        }
        campaignRepo.persist(camp)
        return camp
    }

    @Transactional
    fun createCampaignObject(
        id: UUID,
        campaign: Campaign,
        settingObj: SettingObject,
        gmId: UUID,
        template: Template
    ): CampaignObject {
        val gm = gmRepo.findById(gmId)
        val obj = CampaignObject().apply {
            this.id = id
            title = "co"
            this.campaign = campaign
            this.settingObject = settingObj
            this.template = template
            this.gm = gm
            payload = "{}"
            createdAt = java.time.Instant.now()  // Setting the required createdAt field
        }
        campaignObjectRepo.persist(obj)
        return obj
    }

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

    @Test
    @TestSecurity(user = "userJwt", roles = ["viewer"])
    @JwtSecurity(
        claims = [
            Claim(key = "email", value = "user@gmail.com"),
            Claim(key = "sub", value = "userJwt"),
            Claim(key = "gmId", value = "00000000-0000-0000-0000-000000000001")
        ]
    )
    fun patchObject_success() {
        val gmId = testGmId1
        // createGm(gmId) -- now handled in @BeforeEach
        val setting = createSetting(UUID.randomUUID(), gmId)
        val template = createTemplate(UUID.randomUUID(), gmId, "{}")
        val settingObj = createSettingObject(UUID.randomUUID(), setting, template, gmId)
        val campaign = createCampaign(UUID.randomUUID(), gmId, setting)
        val campObj = createCampaignObject(UUID.randomUUID(), campaign, settingObj, gmId, template)

        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"new\"}")
            .`when`().patch("/api/campaigns/${campaign.id}/objects/${campObj.id}")
            .then().statusCode(200)
            .body("id", equalTo(campObj.id.toString()))

        verifyPayload(campObj.id!!, "{\"name\": \"new\"}")
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
    fun patchObject_validationFailure() {
        val gmId = testGmId1
        val setting = createSetting(UUID.randomUUID(), gmId)
        val template = createTemplate(
            UUID.randomUUID(),
            gmId,
            "{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"}},\"required\":[\"name\"]}"
        )
        val settingObj = createSettingObject(UUID.randomUUID(), setting, template, gmId)
        val campaign = createCampaign(UUID.randomUUID(), gmId, setting)
        val campObj = createCampaignObject(UUID.randomUUID(), campaign, settingObj, gmId, template)

        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":1}")
            .`when`().patch("/api/campaigns/${campaign.id}/objects/${campObj.id}")
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
        val setting = createSetting(UUID.randomUUID(), gm1)
        val template = createTemplate(UUID.randomUUID(), gm1, "{}")
        val settingObj = createSettingObject(UUID.randomUUID(), setting, template, gm1)
        val campaign = createCampaign(UUID.randomUUID(), gm1, setting)
        val campObj = createCampaignObject(UUID.randomUUID(), campaign, settingObj, gm1, template)

        given()
            .contentType(ContentType.JSON)
            .body("{}")
            .`when`().patch("/api/campaigns/${campaign.id}/objects/${campObj.id}")
            .then().statusCode(404)
    }

    @Transactional
    fun verifyPayload(id: UUID, expected: String) {
        val obj = campaignObjectRepo.findById(id)
        org.junit.jupiter.api.Assertions.assertEquals(expected, obj!!.payload)
    }
}
