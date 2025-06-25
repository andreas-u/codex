package org.fg.ttrpg

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.quarkus.test.security.jwt.Claim
import io.quarkus.test.security.jwt.JwtSecurity
import io.restassured.RestAssured.given
import org.fg.ttrpg.testutils.IntegrationTestHelper
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

@QuarkusTest
class TemplateResourceIT : IntegrationTestHelper() {

    val gmId = UUID.fromString("00000000-0000-0000-0000-000000000001")
    lateinit var userId: UUID

    companion object {
        const val USER_ID = "d5bbc73c-1e43-5cd6-919c-af383f4fe05a"
    }

    @BeforeEach
    fun setup() {
        createGm(gmId)
        userId = UUID.fromString(USER_ID)
    }


    @Test
    @TestSecurity(user = "userJwt", roles = ["viewer"])
    @JwtSecurity(
        claims = [
            Claim(key = "email", value = "user@gmail.com"),
            Claim(key = "sub", value = "userJwt"),
            Claim(key = "userId", value = USER_ID)
        ]
    )
    fun listByGenre() {
        val tpl = createTemplate(UUID.randomUUID(), gmId, "{}", "npc")
        createTemplate(UUID.randomUUID(), gmId, "{}", "item")

        given()
            .`when`().get("/api/templates?genre=${tpl.genre!!.id}")
            .then().statusCode(200)
            .body("size()", equalTo(1))
    }

    @Test
    @TestSecurity(user = "userJwt", roles = ["viewer"])
    @JwtSecurity(
        claims = [
            Claim(key = "email", value = "user@gmail.com"),
            Claim(key = "sub", value = "userJwt"),
            Claim(key = "userId", value = USER_ID)
        ]
    )
    fun listByType() {
        createTemplate(UUID.randomUUID(), gmId, "{}", "npc")
        createTemplate(UUID.randomUUID(), gmId, "{}", "item")

        given()
            .`when`().get("/api/templates?type=npc")
            .then().statusCode(200)
            .body("size()", equalTo(1))
    }
}
