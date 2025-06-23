-- Enable pgcrypto for gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE gm (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255)
);

CREATE TABLE setting (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE genre (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL
);

CREATE TABLE setting_genre (
    setting_id UUID NOT NULL REFERENCES setting(id),
    genre_id UUID NOT NULL REFERENCES genre(id),
    PRIMARY KEY (setting_id, genre_id)
);

CREATE TABLE template (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    schema JSONB,
    setting_id UUID NOT NULL REFERENCES setting(id)
);
CREATE INDEX template_schema_gin_idx ON template USING GIN (schema);

CREATE TABLE setting_object (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    payload JSONB,
    template_id UUID REFERENCES template(id),
    setting_id UUID NOT NULL REFERENCES setting(id)
);
CREATE INDEX setting_object_payload_gin_idx ON setting_object USING GIN (payload);

CREATE TABLE setting_object_tags (
    setting_object_id UUID NOT NULL REFERENCES setting_object(id),
    tag VARCHAR(255) NOT NULL
);

CREATE TABLE campaign (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    gm_id UUID NOT NULL REFERENCES gm(id),
    setting_id UUID NOT NULL REFERENCES setting(id)
);

CREATE TABLE campaign_object (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    campaign_id UUID NOT NULL REFERENCES campaign(id),
    setting_object_id UUID NOT NULL REFERENCES setting_object(id)
);
