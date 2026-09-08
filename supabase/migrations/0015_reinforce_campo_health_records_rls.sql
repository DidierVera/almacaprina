-- El error "new row violates row-level security policy for table health_records" (42501) al
-- guardar una novedad desde Campo indica que las políticas campo_insert/campo_read de
-- health_records de la migración 0002 no quedaron creadas en este proyecto de Supabase —
-- las de weight_records/milk_production_records sí están (ordeño y pesada ya funcionan para
-- Campo), así que probablemente ese bloque específico se saltó al correr 0002 manualmente.
-- drop + create es seguro de re-ejecutar aunque ya existan.

drop policy if exists "campo_insert_health_records" on public.health_records;
create policy "campo_insert_health_records" on public.health_records
  for insert with check (public.current_user_role() = 'campo');

drop policy if exists "campo_read_health_records" on public.health_records;
create policy "campo_read_health_records" on public.health_records
  for select using (public.current_user_role() = 'campo');
