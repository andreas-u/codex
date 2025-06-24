CREATE TABLE campaign_event_override (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id UUID NOT NULL REFERENCES campaign(id) ON DELETE CASCADE,
    base_event_id UUID REFERENCES timeline_event(id) ON DELETE CASCADE,
    override_mode VARCHAR(16) NOT NULL,
    payload JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
