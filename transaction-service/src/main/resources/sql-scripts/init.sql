/*
PostgreSQL script:
*/

\connect postgres

drop database if exists myapp2 with (force);
create database myapp2;

\connect myapp2

drop schema if exists myapp2;
create schema myapp2;

set search_path = myapp2, public;

-- drop table if exists account;
-- create table account
-- (
--     id             bigserial primary key,
--     account_number bigint      not null,
--     balance        numeric     not null,
--     currency       varchar(50) not null,
--     account_type   varchar(50) not null,
--     status         varchar(50) not null,
--     open_date      date        not null,
--     customer_id    bigint      not null,
--     constraint account_number_unique unique (account_number)
-- );

drop table if exists transaction;
create table transaction
(
    id               bigserial primary key,
    amount           numeric,
    transaction_time timestamp,
    transaction_type varchar(50) not null,
    account_id       bigint      not null,
    constraint fk_account foreign key (account_id) references account (id)
);

insert into transaction (amount, transaction_time, transaction_type, account_id)
values ('100.00', '2024-06-07 13:21:02.312872', 'DEPOSIT', 1);