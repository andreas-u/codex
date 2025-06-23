package org.fg.ttrpg.infra.validation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import com.networknt.schema.ValidationMessage
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

/**
 * Validates payloads against template JSON Schema documents using a small LRU cache.
 */
@ApplicationScoped
class TemplateValidator @Inject constructor(
    private val repository: TemplateSchemaRepository
) {
    companion object {
        private const val CACHE_SIZE = 32
    }

    private val mapper = ObjectMapper()
    private val factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)

    private val cache = object : LinkedHashMap<Long, JsonSchema>(CACHE_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, JsonSchema>?) =
            size > CACHE_SIZE
    }

    fun validate(templateId: Long, payload: JsonNode) {
        val schema = cache.computeIfAbsent(templateId) {
            val schemaText = repository.findSchema(templateId)
                ?: throw IllegalArgumentException("Schema not found for template $templateId")
            factory.getSchema(mapper.readTree(schemaText))
        }
        val errors = schema.validate(payload)
        if (errors.isNotEmpty()) {
            throw TemplateValidationException(errors)
        }
    }
}

class TemplateValidationException(val violations: Set<ValidationMessage>) :
    RuntimeException(violations.joinToString("; ") { it.message })
