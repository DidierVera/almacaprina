-- Agrega "gramos" como unidad de venta — necesario para productos derivados que se venden
-- en presentaciones pequeñas (ej. queso, mantequilla) sin tener que expresarlos en kilogramos.

alter type sale_unit add value 'gram';
