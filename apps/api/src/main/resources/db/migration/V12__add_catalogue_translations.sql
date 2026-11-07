ALTER TABLE catalogue_entries ADD COLUMN translations jsonb NOT NULL DEFAULT '{}'::jsonb;
