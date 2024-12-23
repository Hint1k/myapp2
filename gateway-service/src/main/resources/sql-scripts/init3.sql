/* PostgreSQL script: */
/* change "enabled" field to boolean type later */

\connect postgres

drop database if exists myapp2 with (force);
create database myapp2;

\connect myapp2

drop schema if exists myapp2;
create schema myapp2;

set search_path = myapp2, public;

drop table if exists users;
create table users
(
    id              bigserial primary key,
    first_name      varchar(45) not null,
    last_name       varchar(45) not null,
    customer_number bigint      not null,
    username        varchar(45) not null,
    password        varchar(68) not null,
    enabled         integer     not null
);

drop table if exists authorities;
create table authorities
(
    id        bigserial primary key,
    username  varchar(45) not null,
    authority varchar(45) not null,
    user_id   bigserial   not null,
    foreign key (user_id) references users (id)
);

insert into users (id, first_name, last_name, customer_number, username, password, enabled)
values (1, 'Mary', 'Sue', 0, 'manager',
        '$2a$10$U.TJCuMA4c6lka5Xq7i43OK9iDoA1/niZU3Gi6Xez1JzB7wNwvQzu', 1),
       (2, 'Alex', 'Smith', 0, 'admin',
        '$2a$10$U.TJCuMA4c6lka5Xq7i43OK9iDoA1/niZU3Gi6Xez1JzB7wNwvQzu', 1);

insert into authorities (id, username, authority, user_id)
values (1, 'manager', 'ROLE_MANAGER', 1),
       (2, 'admin', 'ROLE_ADMIN', 2);