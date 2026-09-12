-- =====================================================================
-- ALMACAPRINA — Reset de compras (para volver a calcular Costo por litro)
-- =====================================================================
-- Utilidad para correr manualmente en el SQL Editor de Supabase — NO es
-- una migración numerada. Borra TODA la tabla de compras (purchases)
-- para volver a registrarlas desde cero (ej. porque faltó cargar el IVA
-- en varias compras ya guardadas).
--
-- El "Costo por litro" de solo lectura en Ajustes (CostPerLiter) se
-- calcula sumando 3 fuentes: Purchase.total_cost + HealthRecord.cost +
-- FeedingRecord.cost — este script solo vacía la primera, a propósito.
--
-- QUÉ NO TOCA:
--   - health_records / feeding_records → intactos, con su historial
--     completo y los recordatorios (next_suggested_date) de cada cabra.
--     Si luego también quieres poner en null su campo "cost" sin borrar
--     los registros, avisa y se agrega ese bloque aparte.
--   - insumos / packagings (catálogo) → siguen igual. Eso sí,
--     Insumo.last_unit_cost y Packaging.unit_cost van a seguir
--     mostrando el último costo que traía la compra borrada, hasta que
--     registres una compra nueva de ese insumo/envase — no es un dato
--     roto, solo queda "congelado" en el último valor conocido.
--   - packaging_inventories → los movimientos de inventario que ya
--     generaron las compras de envases NO se borran (no existe una
--     referencia directa compra → movimiento). Si además quieres vaciar
--     el inventario de envases para que el stock cuadre con las compras
--     desde cero, descomenta el bloque opcional al final.
--
-- Es seguro volver a correrlo las veces que quieras.
-- =====================================================================

begin;

truncate table purchases;

commit;

-- ---------------------------------------------------------------------
-- OPCIONAL — solo si también quieres vaciar el inventario de envases que
-- generaron esas compras (packaging_inventories, movement_type =
-- 'purchase'), para que el stock de envases empiece de cero junto con
-- las compras. No toca otros movimientos (ej. venta/merma) si los hay.
-- Descomenta para incluirlo.
-- ---------------------------------------------------------------------
-- delete from packaging_inventories where movement_type = 'purchase';
