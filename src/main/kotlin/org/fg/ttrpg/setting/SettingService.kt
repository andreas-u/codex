package org.fg.ttrpg.setting

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.UUID

@ApplicationScoped
class SettingService @Inject constructor(private val repository: SettingRepository) {
    fun listAll(gmId: UUID): List<Setting> = repository.listByGm(gmId)

    fun findById(id: UUID): Setting? = repository.findById(id)

    fun findByIdForGm(id: UUID, gmId: UUID): Setting? = repository.findByIdForGm(id, gmId)

    fun persist(setting: Setting) {
        repository.persist(setting)
    }
}
