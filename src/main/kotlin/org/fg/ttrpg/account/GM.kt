package org.fg.ttrpg.account

import java.util.UUID

class GM {
    var id: UUID? = null
    var username: String? = null
    var email: String? = null
    var campaigns: MutableList<org.fg.ttrpg.campaign.Campaign> = mutableListOf()
}
