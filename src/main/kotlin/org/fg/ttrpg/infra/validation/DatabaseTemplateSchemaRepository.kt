package org.fg.ttrpg.infra.validation

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.fg.ttrpg.setting.TemplateRepository

@ApplicationScoped
class DatabaseTemplateSchemaRepository @Inject constructor(
    private val templates: TemplateRepository
) : TemplateSchemaRepository {
    override fun findSchema(templateId: Long): String? =
        templates.findById(templateId)?.schema
}
