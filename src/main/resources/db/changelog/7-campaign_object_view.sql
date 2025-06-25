CREATE OR REPLACE VIEW campaign_object_union AS
SELECT co.id,
       co.title,
       co.description,
       co.created_at,
       co.campaign_id,
       co.setting_object_id,
       co.gm_id,
       co.template_id,
       co.override_mode,
       co.payload
FROM campaign_object co
UNION
SELECT so.id,
       so.title,
       so.description,
       so.created_at,
       c.id  AS campaign_id,
       so.id AS setting_object_id,
       so.gm_id,
       so.template_id,
       NULL  AS override_mode,
       so.payload
FROM campaign c
         JOIN setting_object so ON so.setting_id = c.setting_id
WHERE NOT EXISTS (SELECT 1
                  FROM campaign_object co2
                  WHERE co2.campaign_id = c.id
                    AND co2.setting_object_id = so.id);
