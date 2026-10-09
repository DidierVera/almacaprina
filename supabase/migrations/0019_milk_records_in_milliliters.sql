-- La unidad canónica del ordeño pasa de litros a mililitros: Campo captura en ml u oz
-- (la báscula pesa en onzas) y la app siempre guarda ml. Los registros existentes se
-- convierten (L × 1000) en el mismo UPDATE de tipo, así no queda ningún dato en litros.
-- total_liters_day era una columna generada: se reemplaza por total_ml_day.
-- Los litros que ven Admin/Ventas se siguen calculando en la app (business/), dividiendo entre 1000.

alter table milk_production_records drop column total_liters_day;

alter table milk_production_records rename column morning_milking_liters to morning_milking_ml;
alter table milk_production_records rename column evening_milking_liters to evening_milking_ml;

alter table milk_production_records
  alter column morning_milking_ml type numeric(10, 1) using round(morning_milking_ml * 1000, 1),
  alter column evening_milking_ml type numeric(10, 1) using round(evening_milking_ml * 1000, 1);

alter table milk_production_records add column total_ml_day numeric(10, 1)
  generated always as (coalesce(morning_milking_ml, 0) + coalesce(evening_milking_ml, 0)) stored;
