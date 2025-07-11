# codex

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./gradlew quarkusDev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./gradlew build
```

It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./gradlew build -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar build/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./gradlew build -Dquarkus.native.enabled=true
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./gradlew build -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./build/codex-1.0.0-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/gradle-tooling>.

## Related Guides

- REST ([guide](https://quarkus.io/guides/rest)): A Jakarta REST implementation utilizing build time processing and Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it.
- REST Jackson ([guide](https://quarkus.io/guides/rest#json-serialisation)): Jackson serialization support for Quarkus REST. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it
- Kotlin ([guide](https://quarkus.io/guides/kotlin)): Write your services in Kotlin
- YAML Configuration ([guide](https://quarkus.io/guides/config-yaml)): Use YAML to configure your Quarkus application
- Liquibase ([guide](https://quarkus.io/guides/liquibase)): Handle your database schema migrations with Liquibase
- JDBC Driver - PostgreSQL ([guide](https://quarkus.io/guides/datasource)): Connect to the PostgreSQL database via JDBC

## Calendar & Timeline API

The service exposes REST endpoints for managing calendars and campaign
timelines.  See `ARCHITECTURE.md` for full details.

```
POST /api/settings/{id}/calendars       - create a calendar
GET  /api/calendars/{id}                - fetch calendar metadata
POST /api/calendars/{id}/events         - add an event
PATCH /api/campaigns/{cid}/events/{eid} - apply an override
GET  /api/campaigns/{cid}/timeline      - list events for a campaign
```

Additional authorization endpoints use bearer tokens with a `userId` claim:

```
POST /api/roles                        - create a role
GET  /api/roles                        - list roles
POST /api/users/{uid}/roles/{rid}      - assign role to user
DELETE /api/users/{uid}/roles/{rid}    - remove role
GET  /api/users/{uid}/roles            - list user roles
POST /api/grants                       - grant object permission
GET  /api/grants/check                 - check permission
```

## Conventions

When referring to entity attributes that represent a human readable label use the column name `title` instead of `name`. This keeps database names consistent with the Kotlin domain model.

## API Documentation

Swagger UI is available when the application runs, providing interactive API docs at `http://localhost:8080/swagger-ui`. The raw OpenAPI specification can be retrieved from `http://localhost:8080/openapi`.
