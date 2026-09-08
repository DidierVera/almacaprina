-- purchases_insumo_xor_packaging exigía que TODA compra tuviera insumo_id o packaging_id
-- (nunca ninguno) — pero "Mano de obra" y "Otro" son gastos, no algo que se consuma de un
-- catálogo con stock, así que ahora la app los guarda sin insumo_id ni packaging_id. Se
-- reemplaza el constraint para aceptar ese tercer caso.

alter table purchases drop constraint purchases_insumo_xor_packaging;

alter table purchases add constraint purchases_insumo_xor_packaging check (
  (category = 'packaging' and packaging_id is not null and insumo_id is null)
  or
  (category in ('labor', 'other') and insumo_id is null and packaging_id is null)
  or
  (category not in ('packaging', 'labor', 'other') and insumo_id is not null and packaging_id is null)
);
