create table orders(
    id uuid primary key default gen_random_uuid(),
    table_number int not null,
    status boolean not null default false,
    draft boolean not null default true,
    name varchar(100) not null,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);