package org.fg.ttrpg.auth

import org.fg.ttrpg.account.GM
import java.util.UUID

class User {
    var id: UUID? = null
    var username: String? = null
    var email: String? = null
    var gm: GM? = null
}
