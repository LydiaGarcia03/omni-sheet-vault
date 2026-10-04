CREATE TABLE players (
    id            uuid PRIMARY KEY,
    subject       text NOT NULL,
    display_name  text NOT NULL,
    created_at    timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX players_subject_key ON players (subject);
