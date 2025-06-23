package org.fg.ttrpg.setting.resource

import com.fasterxml.jackson.databind.JsonNode
import jakarta.ws.rs.WebApplicationException
import org.eclipse.microprofile.jwt.JsonWebToken
import org.fg.ttrpg.account.GMRepository
import org.fg.ttrpg.common.dto.SettingObjectDTO
import org.fg.ttrpg.infra.validation.TemplateSchemaRepository
import org.fg.ttrpg.infra.validation.TemplateValidationException
import org.fg.ttrpg.infra.validation.TemplateValidator
import org.fg.ttrpg.setting.*
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.UUID

class SettingResourceTest {
    private class StubJwt(private val id: UUID) : JsonWebToken {
        override fun getName() = "gm"
        override fun getClaimNames() = setOf("gmId")
        override fun <T : Any?> getClaim(claimName: String?): T? =
            if (claimName == "gmId") id.toString() as T else null
    }

    private class StubValidator : TemplateValidator(object : TemplateSchemaRepository {
        override fun findSchema(templateId: UUID) = null
    }) {
        var called = false
        var throwError = false
        override fun validate(templateId: UUID, payload: JsonNode) {
            called = true
            if (throwError) throw TemplateValidationException(emptySet())
        }
    }

    private class StubService : SettingService(object : SettingRepository(){}) {
        var setting: Setting? = null
        override fun findByIdForGm(id: UUID, gmId: UUID) = setting
    }

    private class StubObjectRepo : SettingObjectRepository() {
        var saved: SettingObject? = null
        override fun persist(entity: SettingObject) { saved = entity }
    }

    private class StubTemplateRepo : TemplateRepository() {
        var template: Template? = null
        override fun findByIdForGm(id: UUID, gmId: UUID) = template
    }

    private val service = StubService()
    private val objectRepo = StubObjectRepo()
    private val templateRepo = StubTemplateRepo()
    private val validator = StubValidator()
    private val jwt = StubJwt(UUID.randomUUID())

    private val resource = SettingResource(service, objectRepo, templateRepo, validator, object : GMRepository(){}, jwt)

    @Test
    fun `validation failure returns 422`() {
        val setting = Setting().apply { id = UUID.randomUUID(); gm = org.fg.ttrpg.account.GM().apply { id = UUID.randomUUID() } }
        service.setting = setting
        val tid = UUID.randomUUID()
        templateRepo.template = Template().apply { id = tid }
        val dto = SettingObjectDTO(null, "slug", "title", payload = "{}", tags = emptyList(), settingId = setting.id!!, templateId = tid)
        validator.throwError = true

        val ex = org.junit.jupiter.api.assertThrows<WebApplicationException> {
            resource.createObject(setting.id!!, dto)
        }
        ex.response.status shouldBe 422
        validator.called shouldBe true
    }

    @Test
    fun `valid payload persists object`() {
        val setting = Setting().apply { id = UUID.randomUUID(); gm = org.fg.ttrpg.account.GM().apply { id = UUID.randomUUID() } }
        service.setting = setting
        val tid = UUID.randomUUID()
        templateRepo.template = Template().apply { id = tid }
        val dto = SettingObjectDTO(null, "slug", "title", payload = "{}", tags = listOf("a"), settingId = setting.id!!, templateId = tid)
        validator.throwError = false

        val result = resource.createObject(setting.id!!, dto)

        objectRepo.saved?.slug shouldBe "slug"
        result.slug shouldBe "slug"
        validator.called shouldBe true
    }
}
