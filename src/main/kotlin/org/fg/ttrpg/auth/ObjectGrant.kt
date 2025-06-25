package org.fg.ttrpg.auth

import java.time.Instant
import java.util.UUID

class ObjectGrant {
    var id: UUID? = null
    var user: User? = null
    var objectId: UUID? = null
    var permission: Permission? = null
    var grantedBy: User? = null
    var grantTime: Instant? = null
}
