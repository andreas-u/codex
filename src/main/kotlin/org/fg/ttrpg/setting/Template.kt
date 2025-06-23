package org.fg.ttrpg.setting

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.time.Instant
import java.util.UUID

@Entity
class Template  {
    @Id
    var id: UUID? = null
    @Column(name = "name")
    var title: String? = null
    var description: String? = null

    @Column(name = "created_at")
    var createdAt: Instant? = null

    /** Type of objects described by this template (e.g. "npc", "item") */
    var type: String? = null

    /** JSON schema describing objects of this template */
    var jsonSchema: String? = null

    @ManyToOne
    var genre: org.fg.ttrpg.genre.Genre? = null

    @ManyToOne
    var gm: org.fg.ttrpg.account.GM? = null
}
