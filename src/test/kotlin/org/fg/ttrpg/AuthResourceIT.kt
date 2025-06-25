package org.fg.ttrpg

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.quarkus.test.security.jwt.Claim
import io.quarkus.test.security.jwt.JwtSecurity
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import jakarta.inject.Inject
import org.fg.ttrpg.auth.Permission
import org.fg.ttrpg.common.dto.RoleDTO
import org.fg.ttrpg.common.dto.GrantDTO
import org.fg.ttrpg.common.dto.SettingDTO
import org.fg.ttrpg.testutils.IntegrationTestHelper
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

@QuarkusTest
class AuthResourceIT : IntegrationTestHelper() {

    val gmId = UUID.fromString("00000000-0000-0000-0000-000000000050")
    lateinit var userId: UUID

    companion object {
        const val USER_ID = "5721e265-1680-5f29-a9b5-a5c6d1ffd0d2"
    }

    @BeforeEach
    fun setup() {
        createGm(gmId)
        userId = UUID.fromString(USER_ID)
        permissionRepo.persist(Permission().apply {
            id = UUID.randomUUID()
            code = "FULL"
        })
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
    fun assignRoleAndGrant() {
        val roleId = given()
            .contentType(ContentType.JSON)
            .body(RoleDTO(null, "editor"))
            .`when`().post("/api/roles")
            .then().statusCode(200)
            .extract().path<String>("id")

        given()
            .`when`().post("/api/users/$userId/roles/$roleId")
            .then().statusCode(204)

        val settingId = given()
            .contentType(ContentType.JSON)
            .body(SettingDTO(null, "World", null, gmId))
            .`when`().post("/api/settings")
            .then().statusCode(200)
            .extract().path<String>("id")

        given()
            .contentType(ContentType.JSON)
            .body(GrantDTO(null, userId, UUID.fromString(settingId), "FULL", userId))
            .`when`().post("/api/grants")
            .then().statusCode(200)

        given()
            .`when`().get("/api/grants/check?userId=$userId&objectId=$settingId&code=FULL")
            .then().statusCode(200)
            .body(equalTo("true"))
    }
}
