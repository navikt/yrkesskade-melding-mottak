ALTER TABLE task DROP CONSTRAINT task_payload_key;

ALTER TABLE task ADD COLUMN payload_hash TEXT NOT NULL DEFAULT '';
UPDATE task SET payload_hash = md5(payload);

ALTER TABLE task ADD CONSTRAINT payload_hash_key UNIQUE (payload_hash);