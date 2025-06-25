# TTRPG Campaign Manager – Backend Architecture & Technical Documentation

> **Document status:** Draft v0.1 – 2025‑06‑23  (ready for team review and inline comments)

---

## 1  Purpose & Scope

A concise, self‑contained reference for architects and developers building the *TTRPG Campaign Manager* backend.  It captures high‑level design choices, domain vocabulary, data flow, validation strategy, and operational guidelines.  Code snippets are deliberately omitted; implementation details will live in source repositories and ADRs.

---

## 2  System Overview

```
┌───────┐   REST/JSON    ┌──────────────────────────┐   JDBI   ┌───────────────┐
│ Client│ ⇆  API Layer ⇆ │   Service / Use‑Cases    │ ⇆ Persistence ⇆ │   Postgres    │
└───────┘                 └──────────────────────────┘                 └───────────────┘
                                           ▲
                                           │ JSON‑Schema validation & merge
                                           ▼
                                      Template Engine
```

* **Quarkus 3 + Kotlin** runtime, Jakarta REST layer.
* **PostgreSQL 16** with JSONB for flexible object payloads.
* **Liquibase** drives schema migrations.
* **JDBI** is configured via `src/main/kotlin/org/fg/ttrpg/infra/JdbiConfig.kt` to install Kotlin and Postgres plugins.
* **JSON Schema** powers user‑defined templates; kotlinx‑serialization optional for hot‑path types.

---

## 3  Core Domain Model

| Entity             | Purpose                          | Key Attributes                                                                           |
| ------------------ | -------------------------------- | ---------------------------------------------------------------------------------------- |
| **GM**             | Account boundary / tenant        | `id`, `email`, `username`, auth fields                                                   |
| **Setting**        | Cohesive world bible             | `id`, `gmId`, `title`, `description`, `createdAt`                                       |
| **Genre**          | Flavour pack attached to Setting | `id`, `settingId`, `code` (`"scifi"`), `name`                                            |
| **Template**       | Declarative schema for objects   | `id`, `genreId`, `type`, `jsonSchema` (JSONB)                                             |
| **SettingObject**  | Canon facts                      | `id`, `settingId`, `templateId?`, `slug`, `payload` (JSONB)                             |
| **SettingObjectTag** | Tag assigned to a SettingObject | `settingObjectId`, `tag` |
| **Campaign**       | Playthrough within Setting       | `id`, `settingId`, `title`, `status`, `startedOn`                                        |
| **CampaignObject** | Override or new object           | `id`, `campaignId`, `settingObjectId?`, `templateId?`, `overrideMode`, `payload` (JSONB) |

*Relationship summary*

* `GM 1‑N Setting`
* `Setting 1‑N Genre 1‑N Template`
* `Setting 1‑N SettingObject`, `Setting 1‑N Campaign`
* `Campaign 1‑N CampaignObject` (optionally links back to a `SettingObject`)

---

## 4  Module & Package Layout

```
org.fg.ttrpg
├── account          (GM auth & multi‑tenancy)
├── setting          (Setting, SettingObject)
├── genre            (Genre, Template)
├── campaign         (Campaign, CampaignObject)
├── calendar         (CalendarSystem)
├── timeline         (TimelineEvent)
├── relationship     (Relationship, RelationshipType)
├── infra
│   ├── merge        (JSON Patch / Merge helpers)
│   └── validation   (schema cache, error mapping)
└── common           (DTOs, mappers, util)
```

Most packages follow an **entity → repository** foundation.  Service and REST resource layers appear only where needed (e.g. `setting`, `campaign`, `calendar`, `timeline`), while lightweight modules such as `account` and `relationship` expose only entities and repositories.  Cross‑cutting utilities sit under `infra`.

---

## 5  Persistence & Migrations

* **Logical DB schema** lives in `src/main/resources/db/changelog/<seq>-<desc>.sql`.
* Key tables – simplified DDL excerpt:

  ```sql
  CREATE TABLE template (
      id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      genre_id    UUID REFERENCES genre(id),
      type        TEXT NOT NULL,
      json_schema JSONB,
      created_at  TIMESTAMPTZ DEFAULT now()
  );

  CREATE TABLE setting_object (
      id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      setting_id  UUID REFERENCES setting(id),
      template_id UUID REFERENCES template(id),
      slug        TEXT UNIQUE,
      payload     JSONB
  );

  CREATE TABLE setting_object_tags (
      setting_object_id UUID REFERENCES setting_object(id) ON DELETE CASCADE,
      tag TEXT NOT NULL
  );
  ```
* **Indexes**

    * `GIN` index on `payload` for full‑text searches.
    * B‑tree index on `setting_object_tags.tag` for quick filtering.
    * Composite `UNION` view to list merged campaign objects efficiently.
* **Audit** (optional MVP+) – triggers populate `<entity>_revision` tables capturing `version`, `changedBy`, `payload`.

---

## 6  Template Lifecycle & Validation

1. **Authoring** – Community or GM uploads a JSON Schema file; it is stored in `template.json_schema`.
2. **Runtime validation pipeline**

    1. API receives object payload + `templateId`.
    2. Template Engine pulls schema from cache (LRU, reload on template change).
    3. Library (Everit / networknt) validates payload → returns error list.
    4. On success, payload is persisted unchanged; on failure, API responds `422` with human‑readable validation report.
3. **Optional compile‑time step** – Gradle plugin generates Kotlin data classes from high‑traffic schemas; services can deserialize via kotlinx‑serialization for performance‑critical reads.

---

## 7  Business Logic & Merge Strategy

### Read

```
Client → CampaignService.getObject(id)
 1. Fetch base SettingObject (if any)
 2. Fetch CampaignObject override
 3. Apply Merge (JSON Merge Patch or custom rules)
 4. Return merged JSON to client
```

### Write

* **World context** – update goes directly to `SettingObject` (after validation).
* **Campaign context** – write a `CampaignObject` with `overrideMode = PATCH | REPLACE | ADD`.
* **Delete inside campaign** – insert tombstone record (`overrideMode = DELETE`, empty payload).

Merge rules live in `infra.merge` and may evolve from RFC 7396 *Merge Patch* to hierarchical rule sets (e.g., numeric stat stacking) as game systems demand.

---

## 8  API Surface (MVP)

| Method                                                | URI                                 | Description                        |
| ----------------------------------------------------- | ----------------------------------- | ---------------------------------- |
| `GET`                                                 | `/api/settings/{id}`                | Retrieve Setting overview          |
| `POST`                                                | `/api/settings`                     | Create new Setting                 |
| `POST`                                                | `/api/settings/{id}/objects`        | Add object to Setting              |
| `GET`                                                 | `/api/campaigns/{id}`               | Summary inc. merged objects        |
| `PATCH`                                               | `/api/campaigns/{id}/objects/{oid}` | Override object                    |
| `GET`                                                 | `/api/templates`                    | Filter templates by `genre`,`type` |
| *Versioning* – add `/v1/` prefix once API stabilises. |                                     |                                    |

---

## 9  Security & Multi‑Tenancy

* **Auth** – Quarkus OIDC bearer tokens (Keycloak / Auth0).  Each request carries `userId` in claims.
* **Data isolation** – every `SELECT` and `UPDATE` clause filters by `gm_id`; JDBI queries bind the tenant id explicitly for each operation.
* **Row Level Security (future)** – activate PostgreSQL RLS for defense‑in‑depth.

---

## 10  Testing & Quality

| Layer       | Strategy                                                           |
| ----------- | ------------------------------------------------------------------ |
| Unit        | JUnit 5, Mockito‑Kotlin; merge logic & validators                  |
| Integration | Quarkus DevServices spins up Testcontainers (Postgres)             |
| Contract    | `rest-assured` + JSONSchema validation of API responses            |
| Performance | Gatling scenario: 50 concurrent GMs listing 1 000 campaign objects |

---

## 11  CI/CD & Deployment

* **CI** – GitHub Actions: build, unit tests, JDK 21, cache Gradle.
* **Container** – Jib builds scratch‑based OCI image.
* **Database migration** – `quarkus.liquibase` runs on pod start; changelogs remain backward‑compatible.
* **Runtime** – Kubernetes or Fly.io; autoscale on CPU.
* **Observability** – Micrometer -> Prometheus + Grafana; OpenTelemetry tracing to Jaeger.

---

## 12  Performance & Scalability Notes

* Hot path = `GET /api/campaigns/{id}`; optimize with:

    * Materialized view holding merged objects snapshot per campaign (refresh async).
    * GZIP HTTP compression.
* JSONB indexed search keeps full‑text look‑ups < 50 ms for 100 k objects.

---

## 13  Roadmap & Extension Points

1. **GraphQL façade** for richer client queries.
2. **Realtime collaboration** via WebSocket channel per campaign.
3. **Plugin system** – GM‑written Kotlin scripts executed in sandbox to auto‑generate lore.
4. **Import/Export** – Markdown → SettingObjects; PDF session logs.

---

## 14  Calendars & Timelines

Calendar and timeline support lets GMs attach events to a Setting and
adapt them per Campaign.  The APIs below are available in the current
service.

A dedicated chronological layer lets GMs plot historical lore, schedule future sessions, and track campaign days without leaking calendar logic into object payloads. The design mirrors the **canon‑vs‑override** strategy already used for Setting & Campaign objects.

### 14.1  Core entities

| Entity                        | Purpose                                         | Key Attributes                                                                                                                    |
| ----------------------------- | ----------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------- |
| **CalendarSystem**            | Defines how dates are measured in a Setting.    | `id`, `settingId`, `name`, `epochLabel` (e.g., "AW"), `months` (JSONB array with `name`, `days`), `leapRule` (JSONB), `createdAt` |
| **TimelineEvent**             | Canon event in the world.                       | `id`, `calendarId`, `title`, `description`, `startDay` (INT), `endDay?` (INT), `objectRefs` (UUID[]), `tags` (TEXT\[])         |
| **EventLink** *(optional v2)* | Relates events causally or hierarchically.      | `id`, `sourceEvent`, `targetEvent`, `type` (`CAUSES`, `PARALLEL`, `CHILD_OF`), `properties` (JSONB)                               |
| **CampaignEventOverride**     | Campaign‑specific add/patch/delete of an event. | `id`, `campaignId`, `baseEventId?`, `overrideMode`, `payload` (JSONB)                                                             |

*Day numbers* are stored as **integer days since calendar epoch**—simple to index and maths‑friendly; UI layers convert to `YYYY‑MM‑DD` strings per system rules.

### 14.2  Integration points

* **Setting scope:** A Setting may own **multiple CalendarSystems** (e.g., Elvish lunar vs Dwarven numeric).
* **Campaign scope:** Every Campaign selects *one primary* CalendarSystem but may reference others for lore translation.
* **Objects ↔ Events:** `TimelineEvent.objectRefs` holds FK list to `setting_object.id`; a join view answers “show every object that appears in timeline Q4” queries.

### 14.3  API endpoints

| Method                                            | URI                                 | Notes |
| ------------------------------------------------- | ----------------------------------- | ----- |
| `POST /api/settings/{id}/calendars`               | Create calendar system              | JSON body `CalendarDTO` |
| `GET /api/calendars/{id}`                         | Calendar metadata & month structure | |
| `POST /api/calendars/{id}/events`                 | Add world‑level event               | JSON body `TimelineEventDTO` |
| `PATCH /api/campaigns/{cid}/events/{eid}`         | Override or delete in campaign      | JSON body `CampaignEventOverrideDTO` |
| `GET /api/campaigns/{cid}/timeline?from=0&to=365` | Merged event list for range         | |
| `POST /api/roles`                                 | Create role                        | JSON body `RoleDTO` |
| `GET /api/roles`                                  | List roles                         | |
| `POST /api/users/{uid}/roles/{rid}`               | Assign role to user                | |
| `DELETE /api/users/{uid}/roles/{rid}`             | Remove role from user              | |
| `GET /api/users/{uid}/roles`                      | List roles for user                | |
| `POST /api/grants`                                | Grant object permission            | JSON body `GrantDTO` |
| `GET /api/grants/check`                           | Check permission                   | |

#### DTO Schemas

```json
// CalendarDTO
{
  "type": "object",
  "properties": {
    "id": {"type": "string", "format": "uuid"},
    "name": {"type": "string"},
    "epochLabel": {"type": "string"},
    "months": {"type": "string"},
    "leapRule": {"type": "string"},
    "settingId": {"type": "string", "format": "uuid"}
  },
  "required": ["name", "settingId"]
}

// TimelineEventDTO
{
  "type": "object",
  "properties": {
    "id": {"type": "string", "format": "uuid"},
    "calendarId": {"type": "string", "format": "uuid"},
    "title": {"type": "string"},
    "description": {"type": "string"},
    "startDay": {"type": "integer"},
    "endDay": {"type": "integer"},
    "objectRefs": {"type": "array", "items": {"type": "string", "format": "uuid"}},
    "tags": {"type": "array", "items": {"type": "string"}}
  },
  "required": ["calendarId", "title", "startDay"]
}

// CampaignEventOverrideDTO
{
  "type": "object",
  "properties": {
    "id": {"type": "string", "format": "uuid"},
    "campaignId": {"type": "string", "format": "uuid"},
    "baseEventId": {"type": "string", "format": "uuid"},
    "overrideMode": {"type": "string"},
    "payload": {"type": "string"}
  },
  "required": ["campaignId", "overrideMode"]
}
```

### 14.4  Storage & indexing

```sql
CREATE TABLE calendar_system (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    setting_id  UUID REFERENCES setting(id),
    name        TEXT NOT NULL,
    epoch_label TEXT,
    months      JSONB NOT NULL,
    leap_rule   JSONB,
    created_at  TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE timeline_event (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    calendar_id UUID REFERENCES calendar_system(id),
    title       TEXT NOT NULL,
    description TEXT,
    start_day   INT NOT NULL,
    end_day     INT,
    object_refs UUID[],
    tags        TEXT[]
);

CREATE INDEX ON timeline_event (calendar_id, start_day);
```

### 14.5  Validation & month arithmetic

* **Months JSONB schema** ensures each month object has `name`, `days`.
* Leap‑year logic coded in Kotlin helper `CalendarMath` (infra) and unit‑tested against schema examples.
* Event payload validation mirrors Template flow.

### 14.6  UI/Client considerations (out‑of‑scope for backend API)

* Gantt‑style timeline, agenda list, “current in‑game date” badge.
* Natural‑language date input parsed client‑side, backend trusts canonical INT days.

---

### Appendix A  Glossary

* **Template** – JSON Schema describing structure of a domain object (e.g., *spaceship*, *spell*, *vampire clan*).
* **Payload** – JSON document conforming to a Template.
* **Override** – Campaign‑specific diff applied on top of a SettingObject.

---

*End of document*
