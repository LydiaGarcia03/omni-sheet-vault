CREATE TABLE characters (
    id                    uuid PRIMARY KEY,
    player_id             uuid NOT NULL REFERENCES players (id),
    system_id             text NOT NULL,
    name                  text NOT NULL,
    portrait_key          text,
    backstory             text,
    sheet                 jsonb NOT NULL,
    sheet_schema_version  int NOT NULL,
    created_at            timestamptz NOT NULL DEFAULT now(),
    updated_at            timestamptz NOT NULL DEFAULT now(),
    deleted_at            timestamptz
);

CREATE INDEX characters_player_id_idx ON characters (player_id);
CREATE INDEX characters_sheet_gin_idx ON characters USING GIN (sheet);
