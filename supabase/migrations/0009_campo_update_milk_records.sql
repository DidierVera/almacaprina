-- Campo puede registrar ordeño (INSERT) y leerlo (SELECT), pero nunca se le dio permiso de
-- UPDATE sobre milk_production_records — por eso corregir una cantidad ya registrada en la
-- sesión de ordeño (MilkingEntryScreen "Editando registro") fallaba con el error de Postgrest
-- "List is empty." (el UPDATE no afectaba ninguna fila por RLS, y el cliente espera una fila
-- de vuelta). Las demás tablas que Campo solo inserta (weight_records, feeding_records,
-- health_records) no tienen un flujo de edición en la UI todavía, así que no se tocan aquí.

create policy "campo_update_milk_records" on public.milk_production_records
  for update
  using (public.current_user_role() = 'campo')
  with check (public.current_user_role() = 'campo');
