package org.fg.ttrpg

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Disabled
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.fg.ttrpg.account.GM
import org.fg.ttrpg.account.GMRepository
import org.fg.ttrpg.campaign.*
import org.fg.ttrpg.setting.*
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test
import io.quarkus.test.TestTransaction
import io.smallrye.jwt.build.Jwt
import java.util.UUID



@Disabled("Tests disabled during build")
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

    private fun token(gmId: UUID) = Jwt.claim("gmId", gmId.toString()).sign()

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
    fun createTemplate(id: UUID, gmId: UUID, schema: String): Template {
        val gm = gmRepo.findById(gmId)
        val template = Template().apply {
            this.id = id
            title = "tpl"
            jsonSchema = schema
            this.gm = gm
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
            this.gm = gm
            this.setting = setting
        }
        campaignRepo.persist(camp)
        return camp
    }

    @Transactional
    fun createCampaignObject(id: UUID, campaign: Campaign, settingObj: SettingObject, gmId: UUID, template: Template): CampaignObject {
        val gm = gmRepo.findById(gmId)
        val obj = CampaignObject().apply {
            this.id = id
            title = "co"
            this.campaign = campaign
            this.settingObject = settingObj
            this.template = template
            this.gm = gm
            payload = "{}"
        }
        campaignObjectRepo.persist(obj)
        return obj
    }

    @Test
    fun patchObject_success() {
        val gmId = UUID.randomUUID()
        createGm(gmId)
        val setting = createSetting(UUID.randomUUID(), gmId)
        val template = createTemplate(UUID.randomUUID(), gmId, "{}")
        val settingObj = createSettingObject(UUID.randomUUID(), setting, template, gmId)
        val campaign = createCampaign(UUID.randomUUID(), gmId, setting)
        val campObj = createCampaignObject(UUID.randomUUID(), campaign, settingObj, gmId, template)

        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"new\"}")
            .auth().oauth2(token(gmId))
            .`when`().patch("/api/campaigns/${campaign.id}/objects/${campObj.id}")
            .then().statusCode(200)
            .body("id", equalTo(campObj.id.toString()))

        verifyPayload(campObj.id!!, "{\"name\":\"new\"}")
    }

    @Test
    fun patchObject_validationFailure() {
        val gmId = UUID.randomUUID()
        createGm(gmId)
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
            .auth().oauth2(token(gmId))
            .`when`().patch("/api/campaigns/${campaign.id}/objects/${campObj.id}")
            .then().statusCode(422)
    }

    @Test
    fun tenantIsolation() {
        val gm1 = UUID.randomUUID()
        val gm2 = UUID.randomUUID()
        createGm(gm1)
        createGm(gm2)
        val setting = createSetting(UUID.randomUUID(), gm1)
        val template = createTemplate(UUID.randomUUID(), gm1, "{}")
        val settingObj = createSettingObject(UUID.randomUUID(), setting, template, gm1)
        val campaign = createCampaign(UUID.randomUUID(), gm1, setting)
        val campObj = createCampaignObject(UUID.randomUUID(), campaign, settingObj, gm1, template)

        given()
            .contentType(ContentType.JSON)
            .body("{}")
            .auth().oauth2(token(gm2))
            .`when`().patch("/api/campaigns/${campaign.id}/objects/${campObj.id}")
            .then().statusCode(404)
    }

    @TestTransaction
    fun verifyPayload(id: UUID, expected: String) {
        val obj = campaignObjectRepo.findById(id)
        org.junit.jupiter.api.Assertions.assertEquals(expected, obj!!.payload)
    }
}
