package org.fg.ttrpg.infra

import com.fasterxml.jackson.databind.ObjectMapper
import org.fg.ttrpg.infra.validation.TemplateSchemaRepository
import org.fg.ttrpg.infra.validation.TemplateValidationException
import org.fg.ttrpg.infra.validation.TemplateValidator
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Disabled
import java.util.UUID


class TemplateValidatorTest {
    private val mapper = ObjectMapper()

    @Test
    fun `invalid payload throws`() {
        val repo = object : TemplateSchemaRepository {
            override fun findSchema(templateId: UUID): String? = """{
                "type": "object",
                "required": ["name"],
                "properties": { "name": {"type": "string"} }
            }"""
        }
        val validator = TemplateValidator(repo)
        val payload = mapper.readTree("{}")
        shouldThrow<TemplateValidationException> {
            validator.validate(UUID.randomUUID(), payload)
        }
    }

    @Test
    fun `valid payload passes`() {
        val repo = object : TemplateSchemaRepository {
            override fun findSchema(templateId: UUID): String? = """{
                "type": "object",
                "properties": { "name": {"type": "string"} }
            }"""
        }
        val validator = TemplateValidator(repo)
        val payload = mapper.readTree("""{"name":"test"}""")
        shouldNotThrowAny {
            validator.validate(UUID.randomUUID(), payload)
        }
    }

    @Test
    fun `missing schema throws`() {
        val repo = object : TemplateSchemaRepository {
            override fun findSchema(templateId: UUID): String? = null
        }
        val validator = TemplateValidator(repo)
        val payload = mapper.readTree("{}")
        shouldThrow<IllegalArgumentException> {
            validator.validate(UUID.randomUUID(), payload)
        }
    }

    @Test
    fun `schema caching prevents duplicate repository calls`() {
        var calls = 0
        val repo = object : TemplateSchemaRepository {
            override fun findSchema(templateId: UUID): String? {
                calls++
                return "{}"
            }
        }
        val validator = TemplateValidator(repo)
        val id = UUID.randomUUID()
        val payload = mapper.readTree("{}")
        validator.validate(id, payload)
        validator.validate(id, payload)
        calls shouldBe 1
    }
}
