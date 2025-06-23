package org.fg.ttrpg.setting

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class SettingRepository : PanacheRepositoryBase<Setting, UUID>
