CREATE TABLE ip_ban
(
    ip         varchar(15)             NOT NULL,
    account_id integer,
    created_at timestamp DEFAULT now() NOT NULL,
    PRIMARY KEY (ip)
);
GRANT SELECT, INSERT ON TABLE ip_ban TO ${server-username};
