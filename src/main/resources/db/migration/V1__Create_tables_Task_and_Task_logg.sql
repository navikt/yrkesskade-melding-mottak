CREATE TABLE IF NOT EXISTS task (
    id            bigserial                                            NOT NULL PRIMARY KEY,
    payload       text                                                 NOT NULL UNIQUE,
    status        varchar      DEFAULT 'UBEHANDLET'::character varying NOT NULL,
    versjon       bigint       DEFAULT 0,
    opprettet_tid timestamp(3) DEFAULT LOCALTIMESTAMP,
    type          varchar                                              NOT NULL,
    metadata      varchar,
    trigger_tid   timestamp    DEFAULT LOCALTIMESTAMP,
    avvikstype    varchar
);

CREATE INDEX IF NOT EXISTS task_status_idx ON task (status);

CREATE TABLE IF NOT EXISTS task_logg (
    id            bigserial    NOT NULL PRIMARY KEY,
    task_id       bigint       NOT NULL REFERENCES task,
    type          varchar      NOT NULL,
    node          varchar      NOT NULL,
    opprettet_tid timestamp(3) DEFAULT LOCALTIMESTAMP,
    melding       text,
    endret_av     varchar      NOT NULL
);

CREATE INDEX IF NOT EXISTS task_logg_task_id_idx ON task_logg (task_id);
