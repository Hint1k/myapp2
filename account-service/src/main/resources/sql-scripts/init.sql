/*
PostgreSQL script:
*/

-- Connect to the postgres database
\connect postgres

drop database if exists myapp2 with (force);
create database myapp2;

-- connect to myapp2 database
\connect myapp2

drop schema if exists myapp2;
create schema myapp2;

set search_path = myapp2, public;

drop table if exists account;
create table account
(
    id             bigserial primary key,
    account_number bigint       not null,
    balance        numeric      not null,
    currency       varchar(255) not null,
    account_type   varchar(255) not null,
    status         varchar(255) not null,
    open_date      date         not null,
    user_id        bigint,
    constraint account_number_unique unique (account_number)
);

drop table if exists transaction;
create table transaction
(
    id               bigserial primary key,
    amount           numeric,
    transaction_time timestamp,
    account_id       bigint not null,
    constraint fk_account foreign key (account_id) references account (id)
);

insert into account (id, account_number, balance, currency, account_type, status, open_date, user_id)
values (1, 1, 0.00, 'USD', 'SAVINGS', 'ACTIVE', '2024-05-24', 1);
