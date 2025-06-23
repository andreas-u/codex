package org.fg.ttrpg.infra.validation

/**
 * Simple abstraction for loading JSON Schema documents for templates.
 */
interface TemplateSchemaRepository {
    fun findSchema(templateId: Long): String?
}
