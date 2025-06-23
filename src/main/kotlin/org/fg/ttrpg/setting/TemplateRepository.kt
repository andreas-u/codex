package org.fg.ttrpg.setting

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class TemplateRepository : PanacheRepository<Template>
