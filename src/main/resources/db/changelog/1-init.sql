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
    description TEXT,
    gm_id UUID REFERENCES gm(id)
);

CREATE TABLE genre (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    setting_id UUID NOT NULL REFERENCES setting(id)
);

CREATE TABLE template (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(255) NOT NULL,
    json_schema JSONB,
    genre_id UUID NOT NULL REFERENCES genre(id)
);
CREATE INDEX template_json_schema_gin_idx ON template USING GIN (json_schema);

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
    setting_object_id UUID NOT NULL REFERENCES setting_object(id),
    template_id UUID REFERENCES template(id),
    override_mode VARCHAR(32),
    payload JSONB
);
