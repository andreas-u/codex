package org.fg.ttrpg

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.junit.jupiter.api.Test

@QuarkusTest
class OpenApiResourceIT {

    @Test
    fun openapiEndpointAccessible() {
        given()
            .`when`().get("/openapi")
            .then()
            .statusCode(200)
    }
}
