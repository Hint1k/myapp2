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
    first_name      varchar(25) not null,
    last_name       varchar(25) not null,
    middle_name     varchar(25) not null,
    email           varchar(25) not null,
    phone           varchar(25) not null,
    account_numbers bigint,      /* accounts can be deleted */
    house_number    varchar(25) not null,
    street          varchar(25) not null,
    city            varchar(25) not null,
    region          varchar(25) not null,
    postal_code     varchar(25) not null,
    country         varchar(25) not null,
    constraint customer_number_unique unique (customer_number)
);

insert into customer (customer_number, first_name, last_name, middle_name, email, phone, account_numbers,
                      house_number, street, city, region, postal_code, country)
values (1, 'John', 'Doe', 'Alex', 'jda@bank.com', '+79999999999', 1,
        '10', 'Bank street', 'Moscow', 'Moscow region', '111111', 'Russia');