
create table categories (
    id uuid primary key default gen_random_uuid(),
    name varchar(255) not null unique,
    description varchar(255) not null,
    created_at timestamp default now(),
    updated_at timestamp default now()
);