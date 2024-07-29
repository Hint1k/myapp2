/* PostgreSQL script: */

\connect postgres

drop database if exists myapp2 with (force);
create database myapp2;

\connect myapp2

drop schema if exists myapp2;
create schema myapp2;

set search_path = myapp2, public;

drop table if exists customer;
create table customer
(
    id              bigserial primary key,
    customer_number bigint      not null,
    name            varchar(25) not null,
    email           varchar(25) not null,
    phone           varchar(25) not null,
    address         varchar(75) not null,
    account_numbers varchar(75),
    constraint customer_number_unique unique (customer_number)
);

insert into customer (customer_number, name, email, phone, address, account_numbers)
values (1, 'John Doe', 'jda@bank.com', '+19999999999', 'Boston, USA', '1');