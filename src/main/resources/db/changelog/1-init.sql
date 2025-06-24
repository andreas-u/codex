-- Enable pgcrypto for gen_random_uuid()
--CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE gm (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255)
);

CREATE TABLE setting (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    gm_id UUID NOT NULL REFERENCES gm(id) ON DELETE CASCADE
);

CREATE TABLE genre (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    code VARCHAR(255) NOT NULL UNIQUE,
    setting_id UUID NOT NULL REFERENCES setting(id) ON DELETE CASCADE
);

CREATE TABLE template (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    type VARCHAR(255) NOT NULL,
    json_schema JSONB,
    genre_id UUID NOT NULL REFERENCES genre(id),
    gm_id UUID NOT NULL REFERENCES gm(id) ON DELETE CASCADE
);
CREATE INDEX template_json_schema_gin_idx ON template USING GIN (json_schema);

CREATE TABLE setting_object (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    payload JSONB,
    template_id UUID REFERENCES template(id) ON DELETE SET NULL,
    setting_id UUID NOT NULL REFERENCES setting(id) ON DELETE CASCADE,
    gm_id UUID NOT NULL REFERENCES gm(id) ON DELETE CASCADE
);
CREATE INDEX setting_object_payload_gin_idx ON setting_object USING GIN (payload);

CREATE TABLE setting_object_tags (
    setting_object_id UUID NOT NULL REFERENCES setting_object(id) ON DELETE CASCADE,
    tag VARCHAR(255) NOT NULL
);

CREATE TABLE campaign (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    started_on TIMESTAMP NOT NULL DEFAULT now(),
    gm_id UUID NOT NULL REFERENCES gm(id) ON DELETE CASCADE,
    setting_id UUID NOT NULL REFERENCES setting(id) ON DELETE CASCADE
);

CREATE TABLE campaign_object (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    campaign_id UUID NOT NULL REFERENCES campaign(id) ON DELETE CASCADE,
    setting_object_id UUID NOT NULL REFERENCES setting_object(id) ON DELETE CASCADE,
    gm_id UUID NOT NULL REFERENCES gm(id) ON DELETE CASCADE,
    template_id UUID REFERENCES template(id) ON DELETE SET NULL,
    override_mode VARCHAR(32),
    payload JSONB
);
