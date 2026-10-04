ALTER TABLE characters
    ADD COLUMN status text NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN creation_draft jsonb,
    ADD CONSTRAINT characters_status_check CHECK (status IN ('DRAFT', 'ACTIVE')),
    ADD CONSTRAINT characters_draft_has_build_check CHECK (status <> 'DRAFT' OR creation_draft IS NOT NULL);
