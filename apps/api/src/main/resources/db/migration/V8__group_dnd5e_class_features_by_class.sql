-- D&D 5e sheets: class and subclass features name their class as source, with no level,
-- and the feature that grants the subclass lists the chosen subclass in "choices".

WITH classes AS (
    SELECT c.id,
           cl ->> 'classSlug'    AS class_slug,
           cl ->> 'className'    AS class_name,
           cl ->> 'subclassSlug' AS subclass_slug,
           cl ->> 'subclassName' AS subclass_name
    FROM characters c
    CROSS JOIN LATERAL jsonb_array_elements(c.sheet -> 'classLevels') cl
    WHERE c.system_id = 'dnd-5e'
      AND jsonb_typeof(c.sheet -> 'classLevels') = 'array'
),
subclass_granting_features AS (
    SELECT ce.slug                            AS class_slug,
           (ce.data ->> 'subclassLevel')::int AS level,
           f ->> 'name'                       AS name
    FROM catalogue_entries ce
    CROSS JOIN LATERAL jsonb_array_elements(ce.data -> 'features') f
    WHERE ce.system_id = 'dnd-5e'
      AND ce.kind = 'CLASS'
      AND (f ->> 'grantsSubclassFeature')::boolean
      AND (f ->> 'level')::int = (ce.data ->> 'subclassLevel')::int
      AND f ->> 'name' NOT LIKE '% feature'
),
rewritten AS (
    SELECT c.id,
           jsonb_agg(
               CASE
                   WHEN t ->> 'category' <> 'CLASS_FEATURE' THEN t
                   ELSE t
                       || jsonb_build_object('source', COALESCE(owner.class_name, t ->> 'source'))
                       || CASE
                              WHEN chosen.subclass_name IS NOT NULL
                                  THEN jsonb_build_object('choices', jsonb_build_array(chosen.subclass_name))
                              ELSE '{}'::jsonb
                          END
               END
               ORDER BY ord) AS traits
    FROM characters c
    CROSS JOIN LATERAL jsonb_array_elements(c.sheet -> 'featureTraits') WITH ORDINALITY AS e(t, ord)
    LEFT JOIN LATERAL (
        SELECT k.class_name
        FROM classes k
        WHERE k.id = c.id
          AND ((split_part(t ->> 'key', ':', 1) = 'class' AND split_part(t ->> 'key', ':', 2) = k.class_slug)
            OR (split_part(t ->> 'key', ':', 1) = 'subclass' AND split_part(t ->> 'key', ':', 2) = k.subclass_slug))
        LIMIT 1
    ) owner ON true
    LEFT JOIN LATERAL (
        SELECT k.subclass_name
        FROM classes k
        JOIN subclass_granting_features s ON s.class_slug = k.class_slug
        WHERE k.id = c.id
          AND k.subclass_name IS NOT NULL
          AND t ->> 'key' LIKE 'class:' || k.class_slug || ':' || s.level || ':%'
          AND t ->> 'name' = s.name
        LIMIT 1
    ) chosen ON true
    WHERE c.system_id = 'dnd-5e'
      AND jsonb_typeof(c.sheet -> 'featureTraits') = 'array'
      AND jsonb_array_length(c.sheet -> 'featureTraits') > 0
    GROUP BY c.id
)
UPDATE characters c
SET sheet = jsonb_set(c.sheet, '{featureTraits}', r.traits)
FROM rewritten r
WHERE r.id = c.id;
