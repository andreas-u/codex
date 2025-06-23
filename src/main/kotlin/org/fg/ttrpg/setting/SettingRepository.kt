package org.fg.ttrpg.setting

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class SettingRepository : PanacheRepositoryBase<Setting, UUID> {
    fun listByGm(gmId: UUID) = list("gm.id", gmId)

    fun findByIdForGm(id: UUID, gmId: UUID): Setting? =
        find("id=?1 and gm.id=?2", id, gmId).firstResult()
}
