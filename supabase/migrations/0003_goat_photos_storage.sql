-- =====================================================================
-- ALMACAPRINA — Storage bucket para fotos de cabras
-- =====================================================================
-- Requiere que 0002_roles_and_rls.sql ya se haya corrido (usa la función
-- public.current_user_role() definida ahí).
--
-- Bucket público de solo-lectura: cualquiera con la URL puede ver la foto
-- (son fotos de referencia del hato, no datos sensibles), pero solo Admin
-- puede subir/reemplazar/borrar — coincide con que "Compras/Admin" es el
-- único rol que crea/edita fichas técnicas de cabras.
-- =====================================================================

insert into storage.buckets (id, name, public)
values ('goat-photos', 'goat-photos', true)
on conflict (id) do nothing;

create policy "goat_photos_public_read"
  on storage.objects for select
  using (bucket_id = 'goat-photos');

create policy "goat_photos_admin_insert"
  on storage.objects for insert
  with check (bucket_id = 'goat-photos' and public.current_user_role() = 'admin');

create policy "goat_photos_admin_update"
  on storage.objects for update
  using (bucket_id = 'goat-photos' and public.current_user_role() = 'admin');

create policy "goat_photos_admin_delete"
  on storage.objects for delete
  using (bucket_id = 'goat-photos' and public.current_user_role() = 'admin');
