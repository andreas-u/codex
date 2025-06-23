package org.fg.ttrpg.setting

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.util.UUID

@Entity
class SettingObject  {
    @Id
    var id: UUID? = null
    var name: String? = null
    var description: String? = null

    @ManyToOne
    var setting: Setting? = null
}
