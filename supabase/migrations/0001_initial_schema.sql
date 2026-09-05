-- ============================================================================
-- Almacaprina — Esquema inicial de base de datos (Supabase / Postgres)
-- Generado a partir de docs/data_model.md
--
-- Convenciones (ver docs/data_model.md § "Convención de nombres"):
--   - Tablas en snake_case plural (Goat -> goats, ProductionBatchInsumoUsage -> production_batch_insumo_usages)
--   - Campos en snake_case
--   - Enums con valores en inglés, minúsculas, snake_case
--
-- Nota de alcance: este script crea tipos enum, tablas, claves foráneas,
-- checks de integridad básicos, índices y los datos semilla obligatorios
-- documentados en el modelo (Product "Leche fresca" y los dos Packaging).
-- No incluye políticas de Row Level Security (se definirán por separado,
-- una vez definidos los 3 roles: campo, ventas, compras/admin) ni las
-- vistas calculadas de la sección "Vistas calculadas" del modelo de datos
-- (DailyHerdProduction, CostPerLiter, etc.) — esas se pueden generar en una
-- migración posterior si se desea.
-- ============================================================================

create extension if not exists pgcrypto; -- para gen_random_uuid()

-- ============================================================================
-- ENUMS
-- ============================================================================

create type goat_sex as enum ('male', 'female');

create type goat_status as enum (
  'kid', 'young_doe', 'in_production', 'pregnant', 'dry',
  'breeding_buck', 'retired', 'deceased'
);

create type goat_origin as enum ('born_on_farm', 'purchased');

create type reproductive_event_type as enum (
  'heat_detected', 'breeding', 'pregnancy_diagnosis', 'birth', 'abortion'
);

create type reproductive_event_result as enum ('pending', 'successful', 'failed');

create type no_milking_reason as enum ('dry', 'sick', 'under_treatment', 'other');

create type health_record_type as enum (
  'vaccine', 'deworming', 'treatment', 'routine_checkup', 'diagnosis'
);

-- Usado en FeedingRecord (sin la opción "all")
create type feeding_animal_group as enum (
  'lactating', 'pregnant', 'young_does', 'dry', 'breeding_bucks', 'general'
);

-- Usado en CareTask (incluye "all", set distinto al de FeedingRecord)
create type care_task_animal_group as enum (
  'lactating', 'pregnant', 'young_does', 'dry', 'breeding_bucks', 'general', 'all'
);

create type insumo_category as enum (
  'feed', 'veterinary', 'processing_input', 'transport', 'labor', 'maintenance', 'other'
);

create type unit_of_measure as enum ('kg', 'g', 'liter', 'ml', 'unit');

create type product_category as enum ('raw_milk', 'derived_dairy');

create type sale_unit as enum ('liter', 'kilogram', 'unit');

-- Igual a insumo_category más 'packaging'
create type purchase_category as enum (
  'feed', 'veterinary', 'packaging', 'processing_input', 'transport', 'labor', 'maintenance', 'other'
);

create type customer_type as enum ('individual', 'business');

create type payment_method as enum ('cash', 'transfer', 'other');

create type payment_status as enum ('paid', 'pending');

create type packaging_movement_type as enum ('purchase', 'sale_use', 'waste');

create type deposit_movement_type as enum ('deposit_charged', 'deposit_returned');

create type care_task_type as enum ('milking', 'feeding', 'medication', 'weighing', 'other');

create type care_task_frequency as enum ('daily', 'specific_days', 'weekly', 'one_time');

create type time_of_day as enum ('morning', 'afternoon', 'both', 'any');

-- ============================================================================
-- 1. goats — entidad central del hato
-- ============================================================================

create table goats (
  id uuid primary key default gen_random_uuid(),
  tag_number text not null,
  name text not null,
  photo_url text,
  sex goat_sex not null,
  breed text,
  breed_cross text[],
  birth_date date,
  mother_id uuid references goats (id) on delete set null,
  father_id uuid references goats (id) on delete set null,
  external_father_description text,
  -- current_status se deriva automáticamente de ReproductiveEvent, excepto en
  -- altas manuales de cabras adultas compradas sin historial (campo explícito
  -- del formulario de alta en ese caso) — la app es responsable de esa lógica.
  current_status goat_status not null,
  current_weight_kg numeric(10, 3),
  current_body_condition_score integer check (current_body_condition_score between 1 and 5),
  herd_entry_date date not null,
  origin goat_origin not null,
  exit_date date,
  exit_reason text,
  notes text
);

create unique index goats_tag_number_key on goats (tag_number);
create index goats_current_status_idx on goats (current_status);

-- ============================================================================
-- 2. weight_records
-- ============================================================================

create table weight_records (
  id uuid primary key default gen_random_uuid(),
  goat_id uuid not null references goats (id) on delete cascade,
  date date not null,
  weight_kg numeric(10, 3) not null,
  body_condition_score integer check (body_condition_score between 1 and 5),
  notes text
);

create index weight_records_goat_id_idx on weight_records (goat_id);
create index weight_records_date_idx on weight_records (date);

-- ============================================================================
-- 3. reproductive_events
-- ============================================================================

create table reproductive_events (
  id uuid primary key default gen_random_uuid(),
  doe_id uuid not null references goats (id) on delete cascade,
  event_type reproductive_event_type not null,
  date date not null,
  buck_id uuid references goats (id) on delete set null,
  -- Gestación de cabra ≈ 150 días (constante de negocio). Solo tiene sentido
  -- cuando event_type = 'breeding'; se calcula igual para toda la fila.
  expected_birth_date date generated always as ((date + interval '150 days')::date) stored,
  result reproductive_event_result not null default 'pending',
  kids_born_count integer,
  kids_alive_count integer,
  -- Array de referencias a Goat (cabritos nacidos). Postgres no soporta FK
  -- nativa sobre elementos de un array: la integridad referencial de estos
  -- ids la garantiza la app al vincular las fichas de los nuevos cabritos.
  kid_ids uuid[],
  notes text
);

create index reproductive_events_doe_id_idx on reproductive_events (doe_id);
create index reproductive_events_buck_id_idx on reproductive_events (buck_id);
create index reproductive_events_date_idx on reproductive_events (date);

-- ============================================================================
-- 4. milk_production_records
-- ============================================================================

create table milk_production_records (
  id uuid primary key default gen_random_uuid(),
  goat_id uuid not null references goats (id) on delete cascade,
  date date not null,
  morning_milking_liters numeric(10, 3),
  evening_milking_liters numeric(10, 3),
  total_liters_day numeric(10, 3)
    generated always as (coalesce(morning_milking_liters, 0) + coalesce(evening_milking_liters, 0)) stored,
  -- days_in_milk depende del último 'birth' exitoso de la cabra (otra tabla):
  -- no se puede derivar como columna generada de esta sola fila, lo calcula la app.
  days_in_milk integer,
  fat_pct numeric(5, 2),
  protein_pct numeric(5, 2),
  no_milking_reason no_milking_reason,
  no_milking_reason_detail text,
  constraint milk_production_records_goat_date_key unique (goat_id, date)
);

create index milk_production_records_date_idx on milk_production_records (date);

-- ============================================================================
-- 7. insumos (creado antes que health_records/feeding_records, que lo referencian)
-- ============================================================================

create table insumos (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  category insumo_category not null,
  unit_of_measure unit_of_measure not null,
  last_unit_cost numeric(12, 2),
  reorder_lead_time_days integer,
  active boolean not null default true
);

create index insumos_category_idx on insumos (category);

-- ============================================================================
-- 5. health_records
-- ============================================================================

create table health_records (
  id uuid primary key default gen_random_uuid(),
  goat_id uuid not null references goats (id) on delete cascade,
  type health_record_type not null,
  date date not null,
  description text,
  -- Nulo si el evento no involucra un insumo físico (ej. revisión de rutina)
  insumo_id uuid references insumos (id) on delete restrict,
  dosage text,
  quantity_used numeric(10, 3),
  milk_withdrawal_days integer,
  cost numeric(12, 2),
  veterinarian text,
  next_suggested_date date
);

create index health_records_goat_id_idx on health_records (goat_id);
create index health_records_insumo_id_idx on health_records (insumo_id);
create index health_records_date_idx on health_records (date);

-- ============================================================================
-- 6. feeding_records
-- ============================================================================

create table feeding_records (
  id uuid primary key default gen_random_uuid(),
  date date not null,
  animal_group feeding_animal_group not null,
  goat_id uuid references goats (id) on delete cascade,
  insumo_id uuid not null references insumos (id) on delete restrict,
  quantity numeric(10, 3) not null,
  cost numeric(12, 2)
);

create index feeding_records_goat_id_idx on feeding_records (goat_id);
create index feeding_records_insumo_id_idx on feeding_records (insumo_id);
create index feeding_records_date_idx on feeding_records (date);

-- ============================================================================
-- 9. products
-- ============================================================================

create table products (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  category product_category not null,
  sale_unit sale_unit not null,
  default_unit_price numeric(12, 2) not null,
  active boolean not null default true
);

-- ============================================================================
-- 10. packagings
-- ============================================================================

create table packagings (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  is_returnable boolean not null,
  -- Nulo si no aplica (envases no retornables)
  deposit_amount numeric(12, 2),
  unit_cost numeric(12, 2) not null,
  constraint packagings_deposit_requires_returnable
    check (not is_returnable or deposit_amount is not null)
);

-- ============================================================================
-- 8. product_recipe_items
-- ============================================================================

create table product_recipe_items (
  id uuid primary key default gen_random_uuid(),
  -- Regla de negocio "product_id debe tener category = derived_dairy" se
  -- valida en la capa de aplicación (un CHECK no puede consultar otra tabla).
  product_id uuid not null references products (id) on delete cascade,
  insumo_id uuid not null references insumos (id) on delete restrict,
  quantity_per_output_unit numeric(10, 3) not null,
  constraint product_recipe_items_product_insumo_key unique (product_id, insumo_id)
);

create index product_recipe_items_product_id_idx on product_recipe_items (product_id);

-- ============================================================================
-- 11. production_batches
-- ============================================================================

create table production_batches (
  id uuid primary key default gen_random_uuid(),
  date date not null,
  -- Regla de negocio "output_product_id debe tener category = derived_dairy"
  -- se valida en la capa de aplicación.
  output_product_id uuid not null references products (id) on delete restrict,
  milk_liters_used numeric(10, 3) not null,
  output_quantity numeric(10, 3) not null,
  yield_ratio numeric(10, 3) generated always as (milk_liters_used / nullif(output_quantity, 0)) stored,
  responsible text,
  notes text
);

create index production_batches_output_product_id_idx on production_batches (output_product_id);
create index production_batches_date_idx on production_batches (date);

-- ============================================================================
-- 12. production_batch_insumo_usages
-- ============================================================================

create table production_batch_insumo_usages (
  id uuid primary key default gen_random_uuid(),
  production_batch_id uuid not null references production_batches (id) on delete cascade,
  insumo_id uuid not null references insumos (id) on delete restrict,
  quantity_used numeric(10, 3) not null,
  -- Copia de Insumo.last_unit_cost al momento del lote: no se actualiza si
  -- luego cambia el costo del insumo, para no distorsionar costos históricos.
  unit_cost_at_time numeric(12, 2) not null,
  cost numeric(12, 2) generated always as (quantity_used * unit_cost_at_time) stored
);

create index production_batch_insumo_usages_batch_id_idx on production_batch_insumo_usages (production_batch_id);
create index production_batch_insumo_usages_insumo_id_idx on production_batch_insumo_usages (insumo_id);

-- ============================================================================
-- 13. purchases
-- ============================================================================

create table purchases (
  id uuid primary key default gen_random_uuid(),
  date date not null,
  category purchase_category not null,
  -- Requerido si category <> 'packaging'
  insumo_id uuid references insumos (id) on delete restrict,
  -- Requerido si category = 'packaging'; al guardar genera movimiento en
  -- packaging_inventories (movement_type = 'purchase') — lógica de aplicación.
  packaging_id uuid references packagings (id) on delete restrict,
  supplier text,
  quantity numeric(10, 3) not null,
  unit text,
  -- Al guardar, actualiza Insumo.last_unit_cost (o el costo del Packaging) — lógica de aplicación.
  unit_cost numeric(12, 2) not null,
  total_cost numeric(12, 2) generated always as (quantity * unit_cost) stored,
  notes text,
  constraint purchases_insumo_xor_packaging check (
    (category = 'packaging' and packaging_id is not null and insumo_id is null)
    or
    (category <> 'packaging' and insumo_id is not null and packaging_id is null)
  )
);

create index purchases_insumo_id_idx on purchases (insumo_id);
create index purchases_packaging_id_idx on purchases (packaging_id);
create index purchases_date_idx on purchases (date);

-- ============================================================================
-- 14. customers
-- ============================================================================

create table customers (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  type customer_type not null,
  contact text,
  address text,
  notes text
);

-- ============================================================================
-- 15. sales
-- ============================================================================

create table sales (
  id uuid primary key default gen_random_uuid(),
  date date not null,
  customer_id uuid not null references customers (id) on delete restrict,
  -- Para ventas de leche, referencia el Product "Leche fresca"
  product_id uuid not null references products (id) on delete restrict,
  quantity_sold numeric(10, 3) not null,
  unit_price numeric(12, 2) not null,
  -- Nulo si el producto se vende a granel sin envase trackeado
  packaging_id uuid references packagings (id) on delete restrict,
  new_packaging_units_count integer not null default 0,
  -- Calculado (new_packaging_units_count * Packaging.deposit_amount): depende
  -- de otra tabla, por lo que no puede ser columna GENERATED; lo calcula la app.
  deposit_charged numeric(12, 2),
  packaging_returned_count integer not null default 0,
  -- Calculado (packaging_returned_count * Packaging.deposit_amount) — igual que arriba.
  deposit_refunded numeric(12, 2),
  -- Calculado ((quantity_sold * unit_price) + deposit_charged - deposit_refunded) — igual que arriba.
  total_value numeric(12, 2),
  -- Si payment_status = 'pending' al crear la venta, se define después al marcarla pagada
  payment_method payment_method,
  payment_status payment_status not null default 'pending',
  paid_date date,
  notes text,
  constraint sales_non_negative_packaging_counts
    check (new_packaging_units_count >= 0 and packaging_returned_count >= 0)
);

create index sales_customer_id_idx on sales (customer_id);
create index sales_product_id_idx on sales (product_id);
create index sales_packaging_id_idx on sales (packaging_id);
create index sales_date_idx on sales (date);
create index sales_payment_status_idx on sales (payment_status);

-- ============================================================================
-- 16. packaging_inventories (movimientos de inventario de envases)
-- ============================================================================

create table packaging_inventories (
  id uuid primary key default gen_random_uuid(),
  date date not null,
  packaging_id uuid not null references packagings (id) on delete restrict,
  movement_type packaging_movement_type not null,
  quantity integer not null,
  -- Por defecto Packaging.unit_cost, editable — lo prellena la app.
  unit_cost numeric(12, 2)
);

create index packaging_inventories_packaging_id_idx on packaging_inventories (packaging_id);
create index packaging_inventories_date_idx on packaging_inventories (date);

-- ============================================================================
-- 17. packaging_deposit_transactions
-- ============================================================================

create table packaging_deposit_transactions (
  id uuid primary key default gen_random_uuid(),
  customer_id uuid not null references customers (id) on delete restrict,
  sale_id uuid not null references sales (id) on delete cascade,
  packaging_id uuid not null references packagings (id) on delete restrict,
  date date not null,
  movement_type deposit_movement_type not null,
  quantity integer not null check (quantity > 0),
  -- Calculado (quantity * Packaging.deposit_amount): depende de otra tabla,
  -- no puede ser columna GENERATED; lo calcula la app.
  amount numeric(12, 2)
);

create index packaging_deposit_transactions_customer_id_idx on packaging_deposit_transactions (customer_id);
create index packaging_deposit_transactions_sale_id_idx on packaging_deposit_transactions (sale_id);
create index packaging_deposit_transactions_packaging_id_idx on packaging_deposit_transactions (packaging_id);

-- ============================================================================
-- 18. business_settings (fila única / singleton)
-- ============================================================================

create table business_settings (
  id uuid primary key default gen_random_uuid(),
  farm_name text not null,
  currency text not null default 'COP',
  -- Meta de producción diaria (hoy 85 L), editable desde el dashboard Admin — nunca hardcodeada en código.
  target_daily_liters_goal numeric(10, 2) not null default 85,
  deposit_alert_days integer not null default 15,
  updated_at date
  -- IMPORTANTE: no agregar aquí un campo de "costo de leche por litro".
  -- Ese valor sale siempre de la vista calculada CostPerLiter y se muestra
  -- de solo lectura en Ajustes — explícitamente descartado como campo editable.
);

-- Garantiza que la tabla tenga como máximo una fila (singleton), sin
-- necesidad de agregar un campo de dominio artificial.
create unique index business_settings_singleton_idx on business_settings ((true));

-- ============================================================================
-- 19. care_tasks
-- ============================================================================

create table care_tasks (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  task_type care_task_type not null,
  frequency care_task_frequency not null,
  animal_group care_task_animal_group,
  -- Opcional, solo si la tarea consume un insumo (feeding, medication)
  insumo_id uuid references insumos (id) on delete restrict,
  quantity_per_occurrence numeric(10, 3),
  time_of_day time_of_day,
  active boolean not null default true
);

create index care_tasks_insumo_id_idx on care_tasks (insumo_id);
create index care_tasks_active_idx on care_tasks (active);

-- ============================================================================
-- 20. care_task_logs
-- ============================================================================

create table care_task_logs (
  id uuid primary key default gen_random_uuid(),
  care_task_id uuid not null references care_tasks (id) on delete cascade,
  date date not null,
  completed boolean not null default false,
  actual_quantity_used numeric(10, 3),
  -- Apunta a un FeedingRecord o HealthRecord generado automáticamente
  -- (según CareTask.task_type). Es una referencia polimórfica hacia dos
  -- tablas distintas: Postgres no soporta una FK única para ese caso, por
  -- lo que la integridad la garantiza la app al generar el registro.
  linked_record_id uuid,
  notes text,
  constraint care_task_logs_task_date_key unique (care_task_id, date)
);

create index care_task_logs_date_idx on care_task_logs (date);

-- ============================================================================
-- Datos semilla obligatorios (ver docs/data_model.md)
-- ============================================================================

insert into products (name, category, sale_unit, default_unit_price, active)
select 'Leche fresca', 'raw_milk', 'liter', 12000, true
where not exists (select 1 from products where name = 'Leche fresca');

insert into packagings (name, is_returnable, deposit_amount, unit_cost)
select 'Botella plástico 1L', false, null, 1200
where not exists (select 1 from packagings where name = 'Botella plástico 1L');

-- packagings.unit_cost es NOT NULL; el modelo no documenta un costo de compra
-- para la botella de vidrio retornable (su valor se recupera vía depósito),
-- así que se semilla en 0 — ajustar si el dueño define un costo real de compra.
insert into packagings (name, is_returnable, deposit_amount, unit_cost)
select 'Botella vidrio 1L', true, 4000, 0
where not exists (select 1 from packagings where name = 'Botella vidrio 1L');
