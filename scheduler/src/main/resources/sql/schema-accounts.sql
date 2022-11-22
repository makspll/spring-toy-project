CREATE TABLE if NOT EXISTS account (
    id SERIAL PRIMARY KEY,
    username varchar(256) NOT NULL,
    password varchar(60),
    permissions smallint NOT NULL DEFAULT 0
);
