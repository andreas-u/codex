plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.allopen") version "2.1.21"
    id("io.quarkus")
    id("nu.studer.jooq") version "9.0"
}

import org.jooq.meta.jaxb.Logging
import org.jooq.meta.jaxb.Property

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project
val jooqVersion: String by project

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-jooq")
    implementation("org.jooq:jooq:${jooqVersion}")
    implementation("io.quarkus:quarkus-liquibase")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-smallrye-jwt")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.quarkus:quarkus-arc")
    implementation("com.networknt:json-schema-validator:1.5.6")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.kotest:kotest-assertions-core:5.8.1")
    testImplementation("io.quarkus:quarkus-jdbc-h2")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.testcontainers:postgresql:1.19.3")
}

group = "org.fg"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
        javaParameters = true
    }
}

jooq {
    version.set(jooqVersion)
    edition.set(nu.studer.gradle.jooq.JooqEdition.OSS)
    configurations {
        create("main") {
            jooqConfiguration.apply {
                logging = Logging.WARN
                generator.apply {
                    database.apply {
                        name = "org.jooq.meta.extensions.liquibase.LiquibaseDatabase"
                        properties.addAll(listOf(
                            Property().apply {
                                key = "scripts"
                                value = "db.changelog-master.yaml"
                            },
                            Property().apply {
                                key = "rootPath"
                                value = "src/main/resources/db/changelog"
                            }
                        ))
                    }
                    target.apply {
                        directory = "${'$'}buildDir/generated-src/jooq"
                    }
                }
            }
        }
    }
}

sourceSets {
    named("main") {
        java.srcDir("${'$'}buildDir/generated-src/jooq")
    }
}
