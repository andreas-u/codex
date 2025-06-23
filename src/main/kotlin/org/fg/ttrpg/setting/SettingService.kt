package org.fg.ttrpg.setting

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.UUID

@ApplicationScoped
class SettingService @Inject constructor(private val repository: SettingRepository) {
    fun listAll(): List<Setting> = repository.listAll()

    fun findById(id: UUID): Setting? = repository.findById(id)

    fun persist(setting: Setting) {
        repository.persist(setting)
    }
}
