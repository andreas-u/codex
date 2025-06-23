package org.fg.ttrpg

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import org.testcontainers.containers.PostgreSQLContainer

class PostgresTestResource : QuarkusTestResourceLifecycleManager {
    private val container = PostgreSQLContainer<Nothing>("postgres:15-alpine")

    override fun start(): Map<String, String> {
        container.start()
        return mapOf(
            "quarkus.datasource.db-kind" to "postgresql",
            "quarkus.datasource.jdbc.url" to container.jdbcUrl,
            "quarkus.datasource.username" to container.username,
            "quarkus.datasource.password" to container.password
        )
    }

    override fun stop() {
        container.stop()
    }
}
