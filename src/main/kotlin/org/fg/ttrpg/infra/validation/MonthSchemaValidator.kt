package org.fg.ttrpg.infra.validation

import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import com.networknt.schema.ValidationMessage
import jakarta.enterprise.context.ApplicationScoped

/** Validates the months JSON structure used by calendar systems. */
@ApplicationScoped
class MonthSchemaValidator {
    private val mapper = ObjectMapper()
    private val schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)
        .getSchema(
            mapper.readTree(
                """{
                   "type": "array",
                   "items": {
                     "type": "object",
                     "required": ["name", "days"],
                     "properties": {
                       "name": {"type": "string"},
                       "days": {"type": "integer", "minimum": 1}
                     }
                   }
                }"""
            )
        )

    fun validate(monthsJson: String) {
        val node = mapper.readTree(monthsJson)
        val errors = schema.validate(node)
        if (errors.isNotEmpty()) {
            throw MonthSchemaValidationException(errors)
        }
    }
}

class MonthSchemaValidationException(val violations: Set<ValidationMessage>) :
    RuntimeException(violations.joinToString("; ") { it.message })
