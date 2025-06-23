package org.fg.ttrpg.campaign

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import java.time.Instant
import java.util.UUID

@Entity
class Campaign  {
    @Id
    var id: UUID? = null
    @Column(name = "name")
    var title: String? = null

    @Column(name = "started_on")
    var startedOn: Instant? = null

    @ManyToOne
    var gm: org.fg.ttrpg.account.GM? = null

    @ManyToOne
    var setting: org.fg.ttrpg.setting.Setting? = null

    @OneToMany(mappedBy = "campaign")
    var objects: MutableList<CampaignObject> = mutableListOf()
}
