package org.fg.ttrpg.setting

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class SettingObjectRepository : PanacheRepositoryBase<SettingObject, UUID> {
    fun listBySettingAndGm(settingId: UUID, gmId: UUID) =
        list("setting.id=?1 and gm.id=?2", settingId, gmId)

    fun listByGm(gmId: UUID) = list("gm.id", gmId)

    fun findByIdForGm(id: UUID, gmId: UUID): SettingObject? =
        find("id=?1 and gm.id=?2", id, gmId).firstResult()
}
