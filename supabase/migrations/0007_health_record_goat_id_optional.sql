-- HealthRecord.goat_id pasa a ser opcional: una CareTask de tipo "medication" es siempre a
-- nivel de GRUPO (nunca de una cabra puntual — ver CLAUDE.md), así que al completarla desde
-- el checklist de Campo se genera un HealthRecord sin goat_id. Los registros individuales
-- (creados desde la ficha técnica de una cabra) siguen llevando su goat_id como siempre.

alter table health_records
  alter column goat_id drop not null;
