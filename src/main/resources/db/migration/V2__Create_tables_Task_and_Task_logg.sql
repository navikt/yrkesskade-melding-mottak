CREATE TABLE task (
    id            bigint                                               NOT NULL PRIMARY KEY,
    payload       text                                                 NOT NULL UNIQUE,
    status        varchar(50)  DEFAULT 'UBEHANDLET'::character varying NOT NULL,
    versjon       bigint       DEFAULT 0,
    opprettet_tid timestamp(3) DEFAULT LOCALTIMESTAMP,
    type          varchar(100)                                         NOT NULL,
    metadata      varchar(4000),
    trigger_tid   timestamp    DEFAULT LOCALTIMESTAMP,
    avvikstype    varchar(50)
);


CREATE SEQUENCE task_seq INCREMENT BY 50 START WITH 1000000 NO CYCLE;
ALTER TABLE task ALTER COLUMN id SET DEFAULT nextval('task_seq');
ALTER SEQUENCE task_seq OWNED BY task.id;

CREATE INDEX ON task (status);

CREATE TABLE task_logg (
    id            bigint       NOT NULL PRIMARY KEY,
    task_id       bigint       NOT NULL REFERENCES task,
    type          varchar(15)  NOT NULL,
    node          varchar(100) NOT NULL,
    opprettet_tid timestamp(3) DEFAULT LOCALTIMESTAMP,
    melding       text,
    endret_av     varchar(100)                                  NOT NULL
);

CREATE SEQUENCE task_logg_seq INCREMENT BY 50 START WITH 1000000 NO CYCLE;
ALTER TABLE task_logg ALTER COLUMN id SET DEFAULT nextval('task_logg_seq');
ALTER SEQUENCE task_logg_seq OWNED BY task_logg.id;
CREATE INDEX ON task_logg (task_id);

