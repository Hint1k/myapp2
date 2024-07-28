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
    constraint customer_number_unique unique (customer_number)
);

drop table if exists customer_accounts;
create table customer_accounts
(
    customer_id    bigint not null,
    account_number bigint not null,
    constraint fk_customer_accounts_customer_id foreign key (customer_id) references customer (id)
);

insert into customer (customer_number, name, email, phone, address)
values (1, 'John Doe', 'jda@bank.com', '+19999999999', 'Boston, USA');

insert into customer_accounts (customer_id, account_number)
values (1, 1);