package org.fg.ttrpg.infra

import com.fasterxml.jackson.databind.ObjectMapper
import org.fg.ttrpg.infra.validation.TemplateSchemaRepository
import org.fg.ttrpg.infra.validation.TemplateValidationException
import org.fg.ttrpg.infra.validation.TemplateValidator
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class TemplateValidatorTest {
    private val mapper = ObjectMapper()

    @Test
    fun `invalid payload throws`() {
        val repo = object : TemplateSchemaRepository {
            override fun findSchema(templateId: Long): String? = """{
                "type": "object",
                "required": ["name"],
                "properties": { "name": {"type": "string"} }
            }"""
        }
        val validator = TemplateValidator(repo)
        val payload = mapper.readTree("{}")
        assertThrows(TemplateValidationException::class.java) {
            validator.validate(1L, payload)
        }
    }
}
