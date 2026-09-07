-- Agrega el concepto opcional de "empaque de compra" al catálogo de insumos,
-- para poder registrar una compra por empaques (ej. "1 botella de 50 ml de cuajo")
-- y que la app calcule quantity/unit_cost en la unidad base (unit_of_measure)
-- sin tocar el cálculo de InsumoStockBalance ni el resto del flujo de Purchase.

alter table insumos
  add column if not exists purchase_package_label text,
  add column if not exists purchase_package_size numeric,
  add column if not exists notes text;
