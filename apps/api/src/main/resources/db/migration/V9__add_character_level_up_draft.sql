-- A level up in progress: the class being leveled and the build with the new level, until it is finished or cancelled.
ALTER TABLE characters ADD COLUMN level_up_draft jsonb;
