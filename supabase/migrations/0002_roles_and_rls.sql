-- =====================================================================
-- ALMACAPRINA — Roles, profiles y Row Level Security
-- =====================================================================
-- Nombres de tabla verificados contra 0001_initial_schema.sql: coinciden
-- exactamente (snake_case plural), no se requirió ningún ajuste.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Rol y tabla de perfiles
-- ---------------------------------------------------------------------
create type user_role as enum ('campo', 'ventas', 'admin');

create table public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  role user_role not null,
  full_name text,
  created_at timestamptz default now()
);

alter table public.profiles enable row level security;

-- Cada usuario puede leer su propio perfil (necesario para que la app
-- sepa a qué Home mandarlo justo después del login)
create policy "read_own_profile"
  on public.profiles for select
  using (auth.uid() = id);

-- ---------------------------------------------------------------------
-- 2. Función helper: rol del usuario actual
-- SECURITY DEFINER evita recursión infinita al consultar profiles
-- desde las políticas de las otras tablas
-- ---------------------------------------------------------------------
create or replace function public.current_user_role()
returns user_role
language sql
security definer
stable
as $$
  select role from public.profiles where id = auth.uid();
$$;

-- ---------------------------------------------------------------------
-- 3. Vincular los 3 usuarios ya creados en el Dashboard con su rol
-- Reemplaza los UUID por los "User UID" reales de cada usuario
-- ---------------------------------------------------------------------
insert into public.profiles (id, role, full_name) values
  ('c2f5ff9d-e3d1-485d-9277-ffe131481d94', 'campo',  'Usuario Campo'),
  ('168f8b2f-e00b-43de-94fb-bd1c66513698', 'ventas', 'Usuario Ventas'),
  ('51cb373d-a236-42b8-ad4b-7dbc6f4ae0ac', 'admin',  'Usuario Admin');

-- =====================================================================
-- 4. Políticas por tabla
-- Patrón: Admin siempre tiene acceso completo. Campo y Ventas solo
-- tocan lo que su módulo realmente necesita (ver CLAUDE.md).
-- =====================================================================

-- ---------- GOATS (Hato) ----------
alter table public.goats enable row level security;

create policy "admin_full_access_goats" on public.goats
  for all using (public.current_user_role() = 'admin')
  with check (public.current_user_role() = 'admin');

create policy "campo_read_goats" on public.goats
  for select using (public.current_user_role() = 'campo');

-- ---------- WEIGHT_RECORDS (Pesadas) ----------
alter table public.weight_records enable row level security;

create policy "admin_full_access_weight_records" on public.weight_records
  for all using (public.current_user_role() = 'admin')
  with check (public.current_user_role() = 'admin');

create policy "campo_insert_weight_records" on public.weight_records
  for insert with check (public.current_user_role() = 'campo');

create policy "campo_read_weight_records" on public.weight_records
  for select using (public.current_user_role() = 'campo');

-- ---------- MILK_PRODUCTION_RECORDS (Ordeño) ----------
alter table public.milk_production_records enable row level security;

create policy "admin_full_access_milk_records" on public.milk_production_records
  for all using (public.current_user_role() = 'admin')
  with check (public.current_user_role() = 'admin');

create policy "campo_insert_milk_records" on public.milk_production_records
  for insert with check (public.current_user_role() = 'campo');

create policy "campo_read_milk_records" on public.milk_production_records
  for select using (public.current_user_role() = 'campo');

-- ---------- CARE_TASKS (Calendario, lo configura Admin) ----------
alter table public.care_tasks enable row level security;

create policy "admin_full_access_care_tasks" on public.care_tasks
  for all using (public.current_user_role() = 'admin')
  with check (public.current_user_role() = 'admin');

create policy "campo_read_care_tasks" on public.care_tasks
  for select using (public.current_user_role() = 'campo');

-- ---------- CARE_TASK_LOGS (Campo marca tareas hechas) ----------
alter table public.care_task_logs enable row level security;

create policy "admin_full_access_care_task_logs" on public.care_task_logs
  for all using (public.current_user_role() = 'admin')
  with check (public.current_user_role() = 'admin');

create policy "campo_insert_care_task_logs" on public.care_task_logs
  for insert with check (public.current_user_role() = 'campo');

create policy "campo_read_care_task_logs" on public.care_task_logs
  for select using (public.current_user_role() = 'campo');

-- ---------- FEEDING_RECORDS (generado al completar tarea de alimentación) ----------
alter table public.feeding_records enable row level security;

create policy "admin_full_access_feeding_records" on public.feeding_records
  for all using (public.current_user_role() = 'admin')
  with check (public.current_user_role() = 'admin');

create policy "campo_insert_feeding_records" on public.feeding_records
  for insert with check (public.current_user_role() = 'campo');

create policy "campo_read_feeding_records" on public.feeding_records
  for select using (public.current_user_role() = 'campo');

-- ---------- HEALTH_RECORDS (generado al completar tarea de medicamento) ----------
alter table public.health_records enable row level security;

create policy "admin_full_access_health_records" on public.health_records
  for all using (public.current_user_role() = 'admin')
  with check (public.current_user_role() = 'admin');

create policy "campo_insert_health_records" on public.health_records
  for insert with check (public.current_user_role() = 'campo');

create policy "campo_read_health_records" on public.health_records
  for select using (public.current_user_role() = 'campo');

-- ---------- INSUMOS (Campo solo necesita leerlos para el checklist) ----------
alter table public.insumos enable row level security;

create policy "admin_full_access_insumos" on public.insumos
  for all using (public.current_user_role() = 'admin')
  with check (public.current_user_role() = 'admin');

create policy "campo_read_insumos" on public.insumos
  for select using (public.current_user_role() = 'campo');

-- ---------- PRODUCTS (Ventas necesita leerlos para elegir producto/precio) ----------
alter table public.products enable row level security;

create policy "admin_full_access_products" on public.products
  for all using (public.current_user_role() = 'admin')
  with check (public.current_user_role() = 'admin');

create policy "ventas_read_products" on public.products
  for select using (public.current_user_role() = 'ventas');

-- ---------- PACKAGINGS (Ventas necesita leerlos para elegir envase) ----------
alter table public.packagings enable row level security;

create policy "admin_full_access_packagings" on public.packagings
  for all using (public.current_user_role() = 'admin')
  with check (public.current_user_role() = 'admin');

create policy "ventas_read_packagings" on public.packagings
  for select using (public.current_user_role() = 'ventas');

-- ---------- CUSTOMERS ----------
alter table public.customers enable row level security;

create policy "admin_full_access_customers" on public.customers
  for all using (public.current_user_role() = 'admin')
  with check (public.current_user_role() = 'admin');

create policy "ventas_full_access_customers" on public.customers
  for all using (public.current_user_role() = 'ventas')
  with check (public.current_user_role() = 'ventas');

-- ---------- SALES ----------
alter table public.sales enable row level security;

create policy "admin_full_access_sales" on public.sales
  for all using (public.current_user_role() = 'admin')
  with check (public.current_user_role() = 'admin');

create policy "ventas_full_access_sales" on public.sales
  for all using (public.current_user_role() = 'ventas')
  with check (public.current_user_role() = 'ventas');

-- ---------- PACKAGING_DEPOSIT_TRANSACTIONS ----------
alter table public.packaging_deposit_transactions enable row level security;

create policy "admin_full_access_deposit_tx" on public.packaging_deposit_transactions
  for all using (public.current_user_role() = 'admin')
  with check (public.current_user_role() = 'admin');

create policy "ventas_full_access_deposit_tx" on public.packaging_deposit_transactions
  for all using (public.current_user_role() = 'ventas')
  with check (public.current_user_role() = 'ventas');

-- =====================================================================
-- 5. Tablas exclusivas de Admin (solo admin tiene políticas —
-- Campo y Ventas no las necesitan para nada de su módulo)
-- =====================================================================
-- reproductive_events, product_recipe_items, production_batches,
-- production_batch_insumo_usages, purchases, packaging_inventories,
-- business_settings

do $$
declare
  t text;
  admin_only_tables text[] := array[
    'reproductive_events',
    'product_recipe_items',
    'production_batches',
    'production_batch_insumo_usages',
    'purchases',
    'packaging_inventories',
    'business_settings'
  ];
begin
  foreach t in array admin_only_tables loop
    execute format('alter table public.%I enable row level security;', t);
    execute format(
      'create policy "admin_full_access_%1$s" on public.%1$s
         for all using (public.current_user_role() = ''admin'')
         with check (public.current_user_role() = ''admin'');',
      t
    );
  end loop;
end $$;
