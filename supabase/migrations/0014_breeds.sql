-- Maestro de razas: catálogo único de nombres de raza (ver CLAUDE.md /
-- business.averageBreedComposition). Antes cada fila de breed_composition se escribía como
-- texto libre, así que dos fichas con la misma raza podían no coincidir exactamente
-- ("Alpina" vs "alpina") y la suma madre+padre quedaba mal repartida. goats.breed_composition
-- sigue guardando el nombre como texto (no una FK) para no romper datos existentes — este
-- catálogo solo estandariza qué nombres se pueden elegir desde el formulario.

create table breeds (
  id uuid primary key default gen_random_uuid(),
  name text not null unique,
  prefix text
);

alter table public.breeds enable row level security;

-- Solo Admin gestiona el catálogo de razas y da de alta/edita cabras.
create policy "admin_full_access_breeds" on public.breeds
  for all using (public.current_user_role() = 'admin')
  with check (public.current_user_role() = 'admin');
