package org.fg.ttrpg.infra

import com.fasterxml.jackson.databind.ObjectMapper
import org.fg.ttrpg.infra.validation.TemplateSchemaRepository
import org.fg.ttrpg.infra.validation.TemplateValidationException
import org.fg.ttrpg.infra.validation.TemplateValidator
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test
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
    fun `uses schema of provided template`() {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val repo = object : TemplateSchemaRepository {
            override fun findSchema(templateId: UUID): String? = when (templateId) {
                id1 -> """{ "type":"object", "required":["a"], "properties":{"a":{"type":"string"}} }"""
                id2 -> """{ "type":"object", "required":["b"], "properties":{"b":{"type":"string"}} }"""
                else -> null
            }
        }
        val validator = TemplateValidator(repo)
        shouldNotThrowAny {
            validator.validate(id1, mapper.readTree("""{"a":"x"}"""))
        }
        shouldThrow<TemplateValidationException> {
            validator.validate(id1, mapper.readTree("""{"b":"x"}"""))
        }
        shouldNotThrowAny {
            validator.validate(id2, mapper.readTree("""{"b":"y"}"""))
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
}
