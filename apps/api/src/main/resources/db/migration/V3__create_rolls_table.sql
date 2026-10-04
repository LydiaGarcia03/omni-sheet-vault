CREATE TABLE rolls (
    id            uuid PRIMARY KEY,
    character_id  uuid NOT NULL REFERENCES characters (id),
    expression    text NOT NULL,
    context       text NOT NULL,
    results       int[] NOT NULL,
    total         int NOT NULL,
    rolled_at     timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX rolls_character_id_rolled_at_idx ON rolls (character_id, rolled_at DESC);
