CREATE TABLE relationship_type
(
    id           UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    setting_id   UUID REFERENCES setting (id),
    code         VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(255),
    directional  BOOLEAN      NOT NULL DEFAULT false,
    schema_json  JSONB,
    created_at   TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE relationship
(
    id               UUID PRIMARY KEY   DEFAULT gen_random_uuid(),
    setting_id       UUID      NOT NULL REFERENCES setting (id),
    type_id          UUID      NOT NULL REFERENCES relationship_type (id),
    source_object    UUID      NOT NULL REFERENCES setting_object (id),
    target_object    UUID      NOT NULL REFERENCES setting_object (id),
    is_bidirectional BOOLEAN   NOT NULL DEFAULT false,
    properties       JSONB,
    created_at       TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX relationship_source_type_idx ON relationship (source_object, type_id);
CREATE INDEX relationship_target_type_idx ON relationship (target_object, type_id);
CREATE INDEX relationship_properties_gin_idx ON relationship USING GIN (properties);

CREATE TABLE relationship_override
(
    id                UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    campaign_id       UUID        NOT NULL REFERENCES campaign (id),
    base_relationship UUID REFERENCES relationship (id),
    override_mode     VARCHAR(16) NOT NULL,
    properties        JSONB,
    created_at        TIMESTAMP   NOT NULL DEFAULT now()
);
