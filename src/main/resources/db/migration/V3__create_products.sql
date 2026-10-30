create table products(
    id uuid primary key default gen_random_uuid(),
    name varchar(100) not null,
    description varchar(255) not null,
    price decimal(10,2) not null,
    category_id uuid not null references categories (id),
    banner varchar(400) not null,
    disabled boolean not null default false,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);
create unique index products_name_lower_idx on products (lower(name));