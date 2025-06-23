CREATE TABLE gm (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255)
);

CREATE TABLE setting (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE genre (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE setting_genre (
    setting_id BIGINT NOT NULL REFERENCES setting(id),
    genre_id BIGINT NOT NULL REFERENCES genre(id),
    PRIMARY KEY (setting_id, genre_id)
);

CREATE TABLE template (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    schema JSONB,
    setting_id BIGINT NOT NULL REFERENCES setting(id)
);
CREATE INDEX template_schema_gin_idx ON template USING GIN (schema);

CREATE TABLE setting_object (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    setting_id BIGINT NOT NULL REFERENCES setting(id)
);

CREATE TABLE campaign (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    gm_id BIGINT NOT NULL REFERENCES gm(id),
    setting_id BIGINT NOT NULL REFERENCES setting(id)
);

CREATE TABLE campaign_object (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    campaign_id BIGINT NOT NULL REFERENCES campaign(id),
    setting_object_id BIGINT NOT NULL REFERENCES setting_object(id)
);
