CREATE TABLE timeline_event
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    calendar_id UUID         NOT NULL REFERENCES calendar_system (id) ON DELETE CASCADE,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    start_day   INT          NOT NULL,
    end_day     INT,
    object_refs UUID[],
    tags        VARCHAR(255)[]
);

CREATE INDEX timeline_event_calendar_start_day_idx ON timeline_event (calendar_id, start_day);
