-- ============================================================================
-- Almacaprina — Initial schema (Supabase / Postgres)
-- Based on docs/business_data_structure.md
-- ============================================================================

create extension if not exists pgcrypto;

-- ----------------------------------------------------------------------------
-- Enumerated types
-- ----------------------------------------------------------------------------

create type user_role as enum ('field', 'sales', 'admin');

create type goat_sex as enum ('male', 'female');

create type goat_status as enum (
  'kid', 'doeling', 'in_production', 'pregnant', 'dry',
  'breeding_buck', 'retired', 'deceased'
);

create type goat_origin as enum ('born_on_farm', 'purchased');

create type reproductive_event_type as enum (
  'heat_detected', 'breeding', 'pregnancy_diagnosis', 'kidding', 'abortion'
);

create type reproductive_event_result as enum ('pending', 'successful', 'failed');

create type health_record_type as enum (
  'vaccination', 'deworming', 'treatment', 'routine_checkup', 'diagnosis'
);

create type animal_group as enum (
  'lactating', 'pregnant', 'doelings', 'dry', 'breeders', 'general'
);

create type supply_category as enum (
  'feed', 'veterinary', 'packaging', 'transport',
  'labor', 'maintenance', 'other'
);

create type customer_type as enum ('individual', 'business');

create type payment_method as enum ('cash', 'transfer', 'other');

create type bottle_movement_type as enum ('purchase', 'used_in_sale', 'waste');

-- ----------------------------------------------------------------------------
-- Utility: generic trigger to maintain updated_at
-- ----------------------------------------------------------------------------

create or replace function public.set_updated_at()
returns trigger
language plpgsql
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

-- ----------------------------------------------------------------------------
-- Profiles (links auth.users to a business role)
-- ----------------------------------------------------------------------------

create table public.profiles (
  id uuid primary key references auth.users (id) on delete cascade,
  full_name text,
  role user_role, -- null until an admin assigns the role
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create trigger trg_profiles_updated_at
  before update on public.profiles
  for each row execute function public.set_updated_at();

-- Automatically creates the profile (without a role) when a new user signs up.
-- An admin must assign the role afterwards from within the app.
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.profiles (id, full_name)
  values (new.id, new.raw_user_meta_data ->> 'full_name');
  return new;
end;
$$;

create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

-- Helper function for RLS policies: role of the currently authenticated user.
-- security definer + fixed search_path to avoid RLS recursion on profiles.
create or replace function public.current_role()
returns user_role
language sql
stable
security definer
set search_path = public
as $$
  select role from public.profiles where id = auth.uid();
$$;

-- ----------------------------------------------------------------------------
-- Goat (central entity)
-- ----------------------------------------------------------------------------

create table public.goats (
  id uuid primary key default gen_random_uuid(),
  tag_number text not null unique,
  name text,
  photo_url text,
  sex goat_sex not null,
  breed text,
  breed_crosses text[],
  birth_date date,
  mother_id uuid references public.goats (id),
  father_id uuid references public.goats (id),
  external_father_description text,
  -- current_status and current_weight_kg/current_body_condition are mirrors
  -- maintained by shared/business (do not edit directly from the UI).
  current_status goat_status not null default 'kid',
  current_weight_kg numeric(6, 2),
  current_body_condition smallint check (current_body_condition between 1 and 5),
  herd_entry_date date,
  origin goat_origin not null,
  exit_date date,
  exit_reason text,
  notes text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index idx_goats_current_status on public.goats (current_status);
create index idx_goats_mother_id on public.goats (mother_id);
create index idx_goats_father_id on public.goats (father_id);

create trigger trg_goats_updated_at
  before update on public.goats
  for each row execute function public.set_updated_at();

-- ----------------------------------------------------------------------------
-- WeightRecord
-- ----------------------------------------------------------------------------

create table public.weight_records (
  id uuid primary key default gen_random_uuid(),
  goat_id uuid not null references public.goats (id),
  date date not null,
  weight_kg numeric(6, 2) not null check (weight_kg > 0),
  body_condition smallint check (body_condition between 1 and 5),
  observations text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index idx_weight_records_goat_id on public.weight_records (goat_id);
create index idx_weight_records_date on public.weight_records (date);

create trigger trg_weight_records_updated_at
  before update on public.weight_records
  for each row execute function public.set_updated_at();

-- ----------------------------------------------------------------------------
-- ReproductiveEvent
-- ----------------------------------------------------------------------------

create table public.reproductive_events (
  id uuid primary key default gen_random_uuid(),
  doe_id uuid not null references public.goats (id),
  event_type reproductive_event_type not null,
  date date not null,
  buck_id uuid references public.goats (id),
  -- Fixed business rule: goat gestation is ~150 days.
  expected_kidding_date date generated always as (
    case when event_type = 'breeding' then date + 150 else null end
  ) stored,
  result reproductive_event_result not null default 'pending',
  kids_born_count integer check (kids_born_count >= 0),
  kids_alive_count integer check (kids_alive_count >= 0),
  -- No referential-integrity FK on the array (Postgres limitation);
  -- validated in shared/business when linking the kids' records.
  kid_ids uuid[],
  observations text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index idx_reproductive_events_doe_id on public.reproductive_events (doe_id);
create index idx_reproductive_events_buck_id on public.reproductive_events (buck_id);
create index idx_reproductive_events_event_type on public.reproductive_events (event_type);

create trigger trg_reproductive_events_updated_at
  before update on public.reproductive_events
  for each row execute function public.set_updated_at();

-- ----------------------------------------------------------------------------
-- MilkProductionRecord
-- ----------------------------------------------------------------------------

create table public.milk_production_records (
  id uuid primary key default gen_random_uuid(),
  goat_id uuid not null references public.goats (id),
  date date not null,
  morning_milking_liters numeric(5, 2) check (morning_milking_liters >= 0),
  evening_milking_liters numeric(5, 2) check (evening_milking_liters >= 0),
  total_daily_liters numeric(5, 2) generated always as (
    coalesce(morning_milking_liters, 0) + coalesce(evening_milking_liters, 0)
  ) stored,
  -- Calculated and maintained by shared/business from the last kidding date.
  days_in_lactation integer,
  fat_pct numeric(4, 2),
  protein_pct numeric(4, 2),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (goat_id, date)
);

create index idx_milk_production_goat_id on public.milk_production_records (goat_id);
create index idx_milk_production_date on public.milk_production_records (date);

create trigger trg_milk_production_updated_at
  before update on public.milk_production_records
  for each row execute function public.set_updated_at();

-- ----------------------------------------------------------------------------
-- HealthRecord
-- ----------------------------------------------------------------------------

create table public.health_records (
  id uuid primary key default gen_random_uuid(),
  goat_id uuid not null references public.goats (id),
  type health_record_type not null,
  date date not null,
  description text,
  product_applied text,
  dosage text,
  milk_withdrawal_days integer check (milk_withdrawal_days >= 0),
  cost numeric(10, 2) check (cost >= 0),
  responsible_veterinarian text,
  next_suggested_date date,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index idx_health_records_goat_id on public.health_records (goat_id);
create index idx_health_records_next_date on public.health_records (next_suggested_date);

create trigger trg_health_records_updated_at
  before update on public.health_records
  for each row execute function public.set_updated_at();

-- ----------------------------------------------------------------------------
-- FeedingRecord
-- ----------------------------------------------------------------------------

create table public.feeding_records (
  id uuid primary key default gen_random_uuid(),
  date date not null,
  animal_group animal_group not null,
  goat_id uuid references public.goats (id),
  feed_type text not null,
  quantity_kg numeric(8, 2) check (quantity_kg >= 0),
  cost numeric(10, 2) check (cost >= 0),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index idx_feeding_records_goat_id on public.feeding_records (goat_id);
create index idx_feeding_records_date on public.feeding_records (date);

create trigger trg_feeding_records_updated_at
  before update on public.feeding_records
  for each row execute function public.set_updated_at();

-- ----------------------------------------------------------------------------
-- Supply / Purchase
-- ----------------------------------------------------------------------------

create table public.supply_purchases (
  id uuid primary key default gen_random_uuid(),
  date date not null,
  category supply_category not null,
  description text,
  supplier text,
  quantity numeric(10, 2) not null check (quantity > 0),
  unit text not null,
  unit_cost numeric(10, 2) not null check (unit_cost >= 0),
  total_cost numeric(12, 2) generated always as (quantity * unit_cost) stored,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index idx_supply_purchases_date on public.supply_purchases (date);
create index idx_supply_purchases_category on public.supply_purchases (category);

create trigger trg_supply_purchases_updated_at
  before update on public.supply_purchases
  for each row execute function public.set_updated_at();

-- ----------------------------------------------------------------------------
-- Customer
-- ----------------------------------------------------------------------------

create table public.customers (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  type customer_type not null default 'individual',
  contact text,
  address text,
  notes text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create trigger trg_customers_updated_at
  before update on public.customers
  for each row execute function public.set_updated_at();

-- ----------------------------------------------------------------------------
-- Sale
-- ----------------------------------------------------------------------------

create table public.sales (
  id uuid primary key default gen_random_uuid(),
  date date not null,
  customer_id uuid not null references public.customers (id),
  liters_sold numeric(6, 2) not null check (liters_sold > 0),
  unit_price numeric(10, 2) not null default 12000 check (unit_price >= 0),
  total_value numeric(12, 2) generated always as (liters_sold * unit_price) stored,
  payment_method payment_method not null default 'cash',
  notes text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index idx_sales_customer_id on public.sales (customer_id);
create index idx_sales_date on public.sales (date);

create trigger trg_sales_updated_at
  before update on public.sales
  for each row execute function public.set_updated_at();

-- ----------------------------------------------------------------------------
-- BottleInventory
-- ----------------------------------------------------------------------------

create table public.bottle_inventory (
  id uuid primary key default gen_random_uuid(),
  date date not null,
  movement_type bottle_movement_type not null,
  quantity integer not null check (quantity > 0),
  unit_cost numeric(10, 2) not null default 1200 check (unit_cost >= 0),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index idx_bottle_inventory_date on public.bottle_inventory (date);
create index idx_bottle_inventory_type on public.bottle_inventory (movement_type);

create trigger trg_bottle_inventory_updated_at
  before update on public.bottle_inventory
  for each row execute function public.set_updated_at();
