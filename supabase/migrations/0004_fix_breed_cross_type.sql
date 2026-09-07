-- =====================================================================
-- ALMACAPRINA — Corrige el tipo de goats.breed_cross
-- =====================================================================
-- docs/data_model.md dejó el tipo de este campo ambiguo ("text/array"). En
-- 0001_initial_schema.sql se eligió text[] (array), pero el modelo de dominio,
-- el repositorio y el formulario de "Nueva cabra" siempre lo trataron como un
-- único texto libre (ej. "Anglosajón", "Alpina x Nubia") — nunca como una
-- lista estructurada. Eso rompía cualquier alta con "Cruces" no vacío:
-- Postgres esperaba un literal de array ("{...}") y recibía una cadena suelta
-- ("malformed array literal").
--
-- Se corrige el esquema para que coincida con cómo se usa realmente en la app
-- (texto libre), en vez de reescribir el formulario a una lista estructurada.
-- =====================================================================

alter table goats
  alter column breed_cross type text using array_to_string(breed_cross, ', ');
