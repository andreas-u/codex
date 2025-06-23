package org.fg.ttrpg.account

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import java.util.UUID

@ApplicationScoped
class GMRepository @Inject constructor(private val dsl: DSLContext) {
    private val GM = DSL.table("gm")
    private val GM_ID = DSL.field("id", java.util.UUID::class.java)
    private val GM_USERNAME = DSL.field("username", String::class.java)
    private val GM_EMAIL = DSL.field("email", String::class.java)

    fun findById(id: UUID): GM? =
        dsl.selectFrom(GM)
            .where(GM_ID.eq(id))
            .fetchOne(::toGM)

    fun persist(gm: GM) {
        dsl.insertInto(GM)
            .set(GM_ID, gm.id)
            .set(GM_USERNAME, gm.username)
            .set(GM_EMAIL, gm.email)
            .execute()
    }

    private fun toGM(record: Record): GM = GM().apply {
        id = record.get(GM_ID)
        username = record.get(GM_USERNAME)
        email = record.get(GM_EMAIL)
    }
}
