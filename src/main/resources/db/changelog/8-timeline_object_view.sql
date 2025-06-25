CREATE OR REPLACE VIEW timeline_object_usage AS
SELECT te.id AS event_id, unnest(te.object_refs) AS setting_object_id
FROM timeline_event te;
