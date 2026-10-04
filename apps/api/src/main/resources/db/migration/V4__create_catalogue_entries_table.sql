CREATE TABLE catalogue_entries (
    id            uuid PRIMARY KEY,
    system_id     text NOT NULL,
    kind          text NOT NULL,
    slug          text NOT NULL,
    name          text NOT NULL,
    source_book   text,
    source_page   int,
    tags          text[] NOT NULL DEFAULT '{}',
    description   text,
    data          jsonb NOT NULL,
    created_at    timestamptz NOT NULL DEFAULT now(),
    updated_at    timestamptz NOT NULL DEFAULT now(),
    UNIQUE (system_id, kind, slug)
);

CREATE INDEX catalogue_entries_system_kind_idx ON catalogue_entries (system_id, kind);
CREATE INDEX catalogue_entries_tags_gin_idx ON catalogue_entries USING GIN (tags);
CREATE INDEX catalogue_entries_data_gin_idx ON catalogue_entries USING GIN (data);
