-- =====================================================================
-- ALMACAPRINA — Reset de datos de prueba a estado inicial
-- =====================================================================
-- Este script NO es una migración numerada (no forma parte del historial
-- 0001..0008) — es una utilidad para correr manualmente en el SQL Editor
-- de Supabase cuando quieras borrar los datos de prueba cargados durante
-- el desarrollo y dejar la base tal como queda justo después de aplicar
-- 0001_initial_schema.sql (solo los 2 envases y el producto "Leche fresca"
-- semilla, sin cabras/compras/ventas/etc.).
--
-- QUÉ NO TOCA (a propósito):
--   - auth.users            → cuentas reales de login, gestionadas por Supabase Auth
--   - public.profiles       → vincula esas cuentas con su rol (campo/ventas/admin);
--                              borrarlas rompería el login de los 3 usuarios reales
--   - public.business_settings → configuración real de la finca (nombre, moneda,
--                              meta diaria) que ya guardaste desde Ajustes — no es
--                              "dato de prueba". Si SÍ quieres vaciarla también,
--                              descomenta la sección al final del script.
--   - Fotos en Supabase Storage (bucket goat-photos) → TRUNCATE no borra archivos
--     del storage, solo filas de tablas. Para borrar fotos de prueba, hazlo desde
--     Dashboard → Storage → goat-photos (selecciona todo → Delete), no por SQL.
--
-- Todo lo demás (cabras, pesadas, eventos reproductivos, producción de leche,
-- insumos, salud, alimentación, catálogo de productos/envases, recetas, lotes
-- de producción, compras, clientes, ventas, movimientos de envase/depósito,
-- tareas de calendario y su registro) se vacía por completo.
--
-- Es seguro volver a correrlo las veces que quieras mientras sigas probando.
-- =====================================================================

begin;

truncate table
  weight_records,
  reproductive_events,
  milk_production_records,
  health_records,
  feeding_records,
  product_recipe_items,
  production_batch_insumo_usages,
  production_batches,
  purchases,
  packaging_deposit_transactions,
  packaging_inventories,
  sales,
  customers,
  care_task_logs,
  care_tasks,
  insumos,
  packagings,
  products,
  goats
cascade;

-- ---------------------------------------------------------------------
-- Vuelve a dejar los 2 envases + el producto "Leche fresca" que exige
-- CLAUDE.md / docs/data_model.md como semilla obligatoria — idéntico al
-- bloque de 0001_initial_schema.sql.
-- ---------------------------------------------------------------------

insert into products (name, category, sale_unit, default_unit_price, active)
values ('Leche fresca', 'raw_milk', 'liter', 12000, true);

insert into packagings (name, is_returnable, deposit_amount, unit_cost)
values ('Botella plástico 1L', false, null, 1200);

insert into packagings (name, is_returnable, deposit_amount, unit_cost)
values ('Botella vidrio 1L', true, 4000, 0);

commit;

-- ---------------------------------------------------------------------
-- OPCIONAL — solo si también quieres vaciar la configuración de Ajustes
-- (nombre de finca, moneda, meta diaria, umbral de alerta de depósito) y
-- que la app vuelva a pedirla desde cero. Descomenta para incluirlo.
-- ---------------------------------------------------------------------
-- truncate table business_settings;
