package org.fg.ttrpg

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.quarkus.test.security.jwt.Claim
import io.quarkus.test.security.jwt.JwtSecurity
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.fg.ttrpg.testutils.IntegrationTestHelper
import org.fg.ttrpg.campaign.Campaign
import org.fg.ttrpg.campaign.CampaignObject
import org.fg.ttrpg.campaign.CampaignObjectRepository
import org.fg.ttrpg.setting.*
import org.hamcrest.CoreMatchers.equalTo
import jakarta.inject.Inject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import jakarta.transaction.Transactional
import io.kotest.matchers.shouldBe
import java.util.*


@QuarkusTest
class CampaignResourceIT : IntegrationTestHelper() {
    @Inject
    lateinit var settingObjectRepo: SettingObjectRepository

    @Inject
    lateinit var campaignObjectRepo: CampaignObjectRepository



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

    @Transactional
    fun createCampaignObjectWithoutTemplate(
        id: UUID,
        campaign: Campaign,
        settingObj: SettingObject,
        gmId: UUID
    ): CampaignObject {
        val gm = gmRepo.findById(gmId)
        val obj = CampaignObject().apply {
            this.id = id
            title = "co"
            this.campaign = campaign
            this.settingObject = settingObj
            this.template = null
            this.gm = gm
            payload = "{}"
            createdAt = java.time.Instant.now()
        }
        campaignObjectRepo.persist(obj)
        return obj
    }

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
            Claim(key = "userId", value = USER_ID1)
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
            Claim(key = "userId", value = USER_ID2)
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

    @Test
    @TestSecurity(user = "userJwt", roles = ["viewer"])
    @JwtSecurity(
        claims = [
            Claim(key = "email", value = "user@gmail.com"),
            Claim(key = "sub", value = "userJwt"),
            Claim(key = "userId", value = USER_ID1)
        ]
    )
    fun getCampaign_success() {
        val gmId = testGmId1
        val setting = createSetting(UUID.randomUUID(), gmId)
        val campaign = createCampaign(UUID.randomUUID(), gmId, setting)

        given()
            .`when`().get("/api/campaigns/${campaign.id}")
            .then().statusCode(200)
            .body("id", equalTo(campaign.id.toString()))
            .body("title", equalTo("camp"))
            .body("status", equalTo("ACTIVE"))
            .body("gmId", equalTo(gmId.toString()))
            .body("settingId", equalTo(setting.id.toString()))
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
    fun getCampaign_notFound() {
        val id = UUID.randomUUID()

        given()
            .`when`().get("/api/campaigns/$id")
            .then().statusCode(404)
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
    fun patchObject_noTemplateUsesSettingObject() {
        val gmId = testGmId1
        val setting = createSetting(UUID.randomUUID(), gmId)
        val template = createTemplate(UUID.randomUUID(), gmId, "{}")
        val settingObj = createSettingObject(UUID.randomUUID(), setting, template, gmId)
        val campaign = createCampaign(UUID.randomUUID(), gmId, setting)
        val campObj = createCampaignObjectWithoutTemplate(UUID.randomUUID(), campaign, settingObj, gmId)

        given()
            .contentType(ContentType.JSON)
            .body("{}")
            .`when`().patch("/api/campaigns/${campaign.id}/objects/${campObj.id}")
            .then().statusCode(200)
            .body("id", equalTo(campObj.id.toString()))
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
    fun patchObject_campaignNotFound() {
        val gmId = testGmId1
        val setting = createSetting(UUID.randomUUID(), gmId)
        val template = createTemplate(UUID.randomUUID(), gmId, "{}")
        val settingObj = createSettingObject(UUID.randomUUID(), setting, template, gmId)

        given()
            .contentType(ContentType.JSON)
            .body("{}")
            .`when`().patch("/api/campaigns/${UUID.randomUUID()}/objects/${settingObj.id}")
            .then().statusCode(404)
    }

    @Transactional
    fun verifyPayload(id: UUID, expected: String) {
        val obj = campaignObjectRepo.findById(id)
        obj!!.payload shouldBe expected
    }
}
