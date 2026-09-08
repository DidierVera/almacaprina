-- Relación muchos-a-muchos Product <-> Packaging: qué envases/empaques aplican a cada
-- producto (ej. leche -> botella plástico/vidrio 1L; queso -> recipiente 250g/500g/1000g).
-- No reemplaza el desacople general Packaging/Product (un envase sigue pudiendo aplicar a
-- varios productos, ej. una bolsa de 500g para cuajada y para mantequilla) — solo agrega el
-- filtro que necesita "Nueva venta" (Ventas) para precargar los envases del producto elegido.

create table product_packaging_options (
  id uuid primary key default gen_random_uuid(),
  product_id uuid not null references products (id) on delete cascade,
  packaging_id uuid not null references packagings (id) on delete cascade,
  is_default boolean not null default false,
  unique (product_id, packaging_id)
);

create index product_packaging_options_product_id_idx on product_packaging_options (product_id);
create index product_packaging_options_packaging_id_idx on product_packaging_options (packaging_id);

alter table public.product_packaging_options enable row level security;

create policy "admin_full_access_product_packaging_options" on public.product_packaging_options
  for all using (public.current_user_role() = 'admin')
  with check (public.current_user_role() = 'admin');

-- Ventas necesita leerlas para precargar el envase correcto en "Nueva venta".
create policy "ventas_read_product_packaging_options" on public.product_packaging_options
  for select using (public.current_user_role() = 'ventas');
