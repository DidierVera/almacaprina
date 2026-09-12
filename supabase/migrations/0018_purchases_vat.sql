-- Permite registrar el IVA de una compra: como porcentaje a sumar (vat_percentage) o
-- marcando que el costo ingresado ya lo incluye (vat_included) — mutuamente excluyentes,
-- reforzado con un check constraint. total_cost pasa a reflejar el costo real pagado
-- (con IVA sumado cuando vat_percentage aplica), que es lo que consume CostPerLiter.

alter table purchases add column vat_percentage numeric(5, 2);
alter table purchases add column vat_included boolean not null default false;

alter table purchases add constraint purchases_vat_mutually_exclusive check (
  not (vat_included and vat_percentage is not null)
);

alter table purchases drop column total_cost;
alter table purchases add column total_cost numeric(12, 2) generated always as (
  case
    when vat_included or vat_percentage is null then quantity * unit_cost
    else quantity * unit_cost * (1 + vat_percentage / 100)
  end
) stored;
