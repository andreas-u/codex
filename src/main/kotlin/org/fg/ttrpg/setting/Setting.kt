package org.fg.ttrpg.setting

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import java.time.Instant
import java.util.UUID

@Entity
class Setting  {
    @Id
    var id: UUID? = null
    @Column(name = "name")
    var title: String? = null
    var description: String? = null

    @Column(name = "created_at")
    var createdAt: Instant? = null

    @ManyToOne
    var gm: org.fg.ttrpg.account.GM? = null

    @ManyToMany
    var genres: MutableList<org.fg.ttrpg.genre.Genre> = mutableListOf()

    @OneToMany(mappedBy = "setting")
    var objects: MutableList<SettingObject> = mutableListOf()
}
