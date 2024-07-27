/* PostgreSQL script: */

\connect postgres

drop database if exists myapp2 with (force);
create database myapp2;

\connect myapp2

drop schema if exists myapp2;
create schema myapp2;

set search_path = myapp2, public;

drop table if exists account;
create table account
(
    id              bigserial primary key,
    account_number  bigint         not null,
    balance         decimal(10, 2) not null,
    currency        varchar(25)    not null,
    account_type    varchar(25)    not null,
    status          varchar(25)    not null,
    open_date       date           not null,
    customer_number bigint         not null,
    constraint account_number_unique unique (account_number)
);

insert into account (account_number, balance, currency, account_type, status, open_date, customer_number)
values (1, 100.00, 'USD', 'SAVINGS', 'ACTIVE', '2024-05-24', 1);