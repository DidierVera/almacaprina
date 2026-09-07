-- Reemplaza los campos de raza en texto libre (breed, breed_cross) por una
-- composición estructurada en porcentajes (100% de una raza o una mezcla),
-- y agrega la fecha de destete. Ver docs/data_model.md § 1. Goat.

alter table goats
  add column if not exists breed_composition jsonb not null default '[]'::jsonb,
  add column if not exists weaning_date date;

-- Migra el mejor esfuerzo del dato existente: si había una raza principal en texto,
-- se traduce a un único componente de 100%. El detalle libre de breed_cross no se
-- puede traducir de forma confiable a porcentajes, así que no se conserva.
update goats
set breed_composition = jsonb_build_array(jsonb_build_object('breed_name', breed, 'percentage', 100))
where breed is not null and breed <> '' and breed_composition = '[]'::jsonb;

alter table goats
  drop column if exists breed,
  drop column if exists breed_cross;
