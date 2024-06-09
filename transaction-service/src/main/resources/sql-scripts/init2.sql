/* PostgreSQL script: */
/* change later to: timestamp with time zone, */

\connect postgres

drop database if exists myapp2 with (force);
create database myapp2;

\connect myapp2

drop schema if exists myapp2;
create schema myapp2;

set search_path = myapp2, public;

drop table if exists transaction;
create table transaction
(
    id                  bigserial primary key,
    amount              decimal(10, 2),
    transaction_time    timestamp,
    transaction_type    varchar(25) not null,
    account_destination bigint      not null
);

insert into transaction (amount, transaction_time, transaction_type, account_destination)
values (100.00, '2024-06-07 13:21:02.312872', 'DEPOSIT', 1);