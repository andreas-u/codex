package org.fg.ttrpg.setting

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.util.UUID

@Entity
class Template  {
    @Id
    var id: UUID? = null
    var name: String? = null
    var description: String? = null

    /** Type of objects described by this template (e.g. "npc", "item") */
    var type: String? = null

    /** JSON schema describing objects of this template */
    var jsonSchema: String? = null

    @ManyToOne
    var genre: org.fg.ttrpg.genre.Genre? = null
}
