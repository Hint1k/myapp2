/*
PostgreSQL script for:
   1) creating user, database, schema
   2) creating tables
   3) filling in the tables
*/

CREATE ROLE "user" WITH LOGIN PASSWORD '123' CREATEDB;
ALTER ROLE "user" SUPERUSER;

CREATE DATABASE myapp2;
CREATE SCHEMA myapp2;

CREATE TABLE myapp2.account
(
    id             SERIAL PRIMARY KEY,
    account_number VARCHAR(255),
    balance        NUMERIC,
    currency       VARCHAR(255),
    account_type   VARCHAR(255),
    status         VARCHAR(255),
    open_date      DATE,
    user_id        BIGINT
);

CREATE TABLE myapp2.transaction
(
    id               SERIAL PRIMARY KEY,
    amount           NUMERIC,
    transaction_time TIMESTAMP,
    account_id       BIGINT REFERENCES myapp2.account (id)
);