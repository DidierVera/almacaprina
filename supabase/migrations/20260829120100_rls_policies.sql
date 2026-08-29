-- ============================================================================
-- Almacaprina — Role-based RLS policies (field / sales / admin)
-- Requires 20260829120000_initial_schema.sql (tables, user_role, current_role())
-- ============================================================================

-- No anonymous usage in this app: only authenticated users with a profile and role.
revoke all on all tables in schema public from anon;

grant usage on schema public to authenticated;
grant select, insert, update, delete on all tables in schema public to authenticated;

-- ----------------------------------------------------------------------------
-- Enable RLS on all business tables
-- ----------------------------------------------------------------------------

alter table public.profiles enable row level security;
alter table public.goats enable row level security;
alter table public.weight_records enable row level security;
alter table public.reproductive_events enable row level security;
alter table public.milk_production_records enable row level security;
alter table public.health_records enable row level security;
alter table public.feeding_records enable row level security;
alter table public.supply_purchases enable row level security;
alter table public.customers enable row level security;
alter table public.sales enable row level security;
alter table public.bottle_inventory enable row level security;

-- ----------------------------------------------------------------------------
-- profiles: each user sees/edits their own; admin manages all (assigns roles)
-- ----------------------------------------------------------------------------

create policy profiles_select_own on public.profiles
  for select using (id = auth.uid() or public.current_role() = 'admin');

create policy profiles_admin_all on public.profiles
  for all using (public.current_role() = 'admin') with check (public.current_role() = 'admin');

-- ----------------------------------------------------------------------------
-- goats: read for all 3 roles; write (create/edit/exit) only admin
-- ----------------------------------------------------------------------------

create policy goats_select_team on public.goats
  for select using (public.current_role() is not null);

create policy goats_admin_all on public.goats
  for all using (public.current_role() = 'admin') with check (public.current_role() = 'admin');

-- ----------------------------------------------------------------------------
-- weight_records: biweekly weigh-ins — Field records, admin has full access
-- ----------------------------------------------------------------------------

create policy weight_records_select_team on public.weight_records
  for select using (public.current_role() is not null);

create policy weight_records_write_field on public.weight_records
  for insert with check (public.current_role() in ('field', 'admin'));

create policy weight_records_update_field on public.weight_records
  for update using (public.current_role() in ('field', 'admin'))
  with check (public.current_role() in ('field', 'admin'));

create policy weight_records_admin_all on public.weight_records
  for all using (public.current_role() = 'admin') with check (public.current_role() = 'admin');

-- ----------------------------------------------------------------------------
-- milk_production_records: daily milking — Field records, admin has full access
-- ----------------------------------------------------------------------------

create policy milk_production_select_team on public.milk_production_records
  for select using (public.current_role() is not null);

create policy milk_production_write_field on public.milk_production_records
  for insert with check (public.current_role() in ('field', 'admin'));

create policy milk_production_update_field on public.milk_production_records
  for update using (public.current_role() in ('field', 'admin'))
  with check (public.current_role() in ('field', 'admin'));

create policy milk_production_admin_all on public.milk_production_records
  for all using (public.current_role() = 'admin') with check (public.current_role() = 'admin');

-- ----------------------------------------------------------------------------
-- reproductive_events, health_records, feeding_records, supply_purchases:
-- read for all 3 roles; write only admin (purchases/admin owns these modules)
-- ----------------------------------------------------------------------------

create policy reproductive_events_select_team on public.reproductive_events
  for select using (public.current_role() is not null);

create policy reproductive_events_admin_all on public.reproductive_events
  for all using (public.current_role() = 'admin') with check (public.current_role() = 'admin');

create policy health_records_select_team on public.health_records
  for select using (public.current_role() is not null);

create policy health_records_admin_all on public.health_records
  for all using (public.current_role() = 'admin') with check (public.current_role() = 'admin');

create policy feeding_records_select_team on public.feeding_records
  for select using (public.current_role() is not null);

create policy feeding_records_admin_all on public.feeding_records
  for all using (public.current_role() = 'admin') with check (public.current_role() = 'admin');

create policy supply_purchases_select_team on public.supply_purchases
  for select using (public.current_role() is not null);

create policy supply_purchases_admin_all on public.supply_purchases
  for all using (public.current_role() = 'admin') with check (public.current_role() = 'admin');

-- ----------------------------------------------------------------------------
-- customers, sales: Sales records; admin has full access
-- ----------------------------------------------------------------------------

create policy customers_select_team on public.customers
  for select using (public.current_role() is not null);

create policy customers_write_sales on public.customers
  for insert with check (public.current_role() in ('sales', 'admin'));

create policy customers_update_sales on public.customers
  for update using (public.current_role() in ('sales', 'admin'))
  with check (public.current_role() in ('sales', 'admin'));

create policy customers_admin_all on public.customers
  for all using (public.current_role() = 'admin') with check (public.current_role() = 'admin');

create policy sales_select_team on public.sales
  for select using (public.current_role() is not null);

create policy sales_write_sales on public.sales
  for insert with check (public.current_role() in ('sales', 'admin'));

create policy sales_update_sales on public.sales
  for update using (public.current_role() in ('sales', 'admin'))
  with check (public.current_role() in ('sales', 'admin'));

create policy sales_admin_all on public.sales
  for all using (public.current_role() = 'admin') with check (public.current_role() = 'admin');

-- ----------------------------------------------------------------------------
-- bottle_inventory: Sales records purchase/usage movements; admin has full access
-- ----------------------------------------------------------------------------

create policy bottle_inventory_select_team on public.bottle_inventory
  for select using (public.current_role() is not null);

create policy bottle_inventory_write_sales on public.bottle_inventory
  for insert with check (public.current_role() in ('sales', 'admin'));

create policy bottle_inventory_update_sales on public.bottle_inventory
  for update using (public.current_role() in ('sales', 'admin'))
  with check (public.current_role() in ('sales', 'admin'));

create policy bottle_inventory_admin_all on public.bottle_inventory
  for all using (public.current_role() = 'admin') with check (public.current_role() = 'admin');
