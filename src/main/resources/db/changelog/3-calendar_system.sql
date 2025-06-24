CREATE TABLE calendar_system (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    setting_id UUID NOT NULL REFERENCES setting(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    epoch_label VARCHAR(255),
    months JSONB NOT NULL,
    leap_rule JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
