package org.fg.ttrpg.genre

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.util.UUID

@Entity
class Genre  {
    @Id
    var id: UUID? = null
    var name: String? = null

    @ManyToOne
    var setting: org.fg.ttrpg.setting.Setting? = null
}
