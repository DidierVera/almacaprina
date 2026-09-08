-- Permite lectura pública (incluso sin sesión de Supabase) de business_settings, para poder
-- mostrar el nombre de la finca en las pantallas de Login y Splash — ahí todavía no existe una
-- sesión autenticada, así que la política existente "admin_full_access_business_settings"
-- (que exige rol admin) no aplica. Ninguna de las columnas de esta tabla (farm_name, currency,
-- target_daily_liters_goal, deposit_alert_days) es información financiera sensible — son datos
-- de branding/configuración general, no montos ni movimientos.
--
-- Esta política SOLO agrega permiso de SELECT para todos (anon + authenticated); la política
-- admin_full_access_business_settings sigue siendo la única que permite INSERT/UPDATE/DELETE.

create policy "public_read_business_settings" on public.business_settings
  for select
  to anon, authenticated
  using (true);
