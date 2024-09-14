CREATE TABLE account
(
    id                 serial                  NOT NULL,
    name               varchar(30)             NOT NULL,
    password           varchar(200)            NOT NULL,
    pin                varchar(4),
    pic                varchar(26),
    logged_in          smallint  DEFAULT 0     NOT NULL,
    created_at         timestamp DEFAULT now() NOT NULL,
    last_login         timestamp,
    birthday           date                    NOT NULL,
    banned             boolean   DEFAULT false NOT NULL,
    banreason          text,
    macs               text,
    nx_credit          integer   DEFAULT 0     NOT NULL,
    maple_point        integer   DEFAULT 0     NOT NULL,
    nx_prepaid         integer   DEFAULT 0     NOT NULL,
    chr_slots          smallint  DEFAULT 3     NOT NULL,
    gender             smallint  DEFAULT 10    NOT NULL,
    temp_ban_timestamp timestamp,
    greason            smallint,
    tos_accepted       boolean   DEFAULT false NOT NULL,
    ip                 text,
    hwid               text,
    PRIMARY KEY (id),
    UNIQUE (name)
);
CREATE UNIQUE INDEX lower_account_name_idx ON "account" (lower(name) );
GRANT SELECT, UPDATE ON TABLE account TO ${server-username};
GRANT USAGE ON SEQUENCE account_id_seq TO ${server-username};
