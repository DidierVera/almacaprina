-- Permite capturar la composición racial de un semental externo (no registrado en el
-- hato) al momento de la monta o del alta manual de una cabra, para poder calcular
-- automáticamente breed_composition de la cría en vez de dejarlo solo con el aporte
-- de la madre. Ver docs/data_model.md § 1. Goat y § 3. ReproductiveEvent.

alter table goats
  add column if not exists external_father_breed_composition jsonb;

alter table reproductive_events
  add column if not exists external_buck_name text,
  add column if not exists external_buck_breed_composition jsonb;
