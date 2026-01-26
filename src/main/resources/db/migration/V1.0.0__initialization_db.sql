create schema core;
create schema security;

create table security.users (
    id uuid not null primary key,
    name varchar(60) not null,
    last_name varchar(60) not null,
    email varchar(100) not null constraint user_unique_email unique,
    password varchar(500) not null,
    is_active boolean default false not null,
    created_at  timestamp not null,
    updated_at  timestamp not null
);