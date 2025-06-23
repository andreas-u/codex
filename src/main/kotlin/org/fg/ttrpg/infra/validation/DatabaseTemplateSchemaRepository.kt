package org.fg.ttrpg.infra.validation

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.fg.ttrpg.setting.TemplateRepository
import java.util.UUID

@ApplicationScoped
class DatabaseTemplateSchemaRepository @Inject constructor(
    private val templates: TemplateRepository
) : TemplateSchemaRepository {
    override fun findSchema(templateId: UUID): String? =
        templates.findById(templateId)?.jsonSchema
}
