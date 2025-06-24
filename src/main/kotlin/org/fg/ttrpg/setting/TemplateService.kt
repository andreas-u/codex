package org.fg.ttrpg.setting

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.UUID

@ApplicationScoped
class TemplateService @Inject constructor(private val repository: TemplateRepository) {
    fun listAll(gmId: UUID): List<Template> = repository.listByGm(gmId)

    fun listByGenre(genre: String): List<Template> = repository.listByGenre(genre)

    fun listByType(type: String): List<Template> = repository.listByType(type)

    fun findById(id: UUID): Template? = repository.findById(id)

    fun findByIdForGm(id: UUID, gmId: UUID): Template? = repository.findByIdForGm(id, gmId)

    fun persist(template: Template) {
        repository.persist(template)
    }
}
