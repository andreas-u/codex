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
    schema JSONB,
    setting_id UUID NOT NULL REFERENCES setting(id)
);
CREATE INDEX template_schema_gin_idx ON template USING GIN (schema);

CREATE TABLE setting_object (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    setting_id UUID NOT NULL REFERENCES setting(id)
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
