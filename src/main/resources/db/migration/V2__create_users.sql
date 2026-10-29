create table users (
    id uuid primary key default gen_random_uuid(),
    name varchar(255) not null,
    email varchar(255) not null unique,
    password varchar(255) not null,
    role varchar(20) not null default 'STAFF',
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);