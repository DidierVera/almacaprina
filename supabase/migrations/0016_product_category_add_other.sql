-- Nueva categoría de producto para líneas de negocio que no son leche ni derivado lácteo
-- (huevos, trucha, pollo) — se venden como SKU simple, sin receta de insumos ni lote de
-- producción que consuma leche cruda (ver CLAUDE.md § Productos derivados y producción).

alter type product_category add value 'other';
