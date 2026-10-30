create table order_items(
    id uuid primary key default gen_random_uuid(),
    amount int not null,
    order_id uuid not null references orders (id) on delete cascade,
    product_id uuid not null references products (id),
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);