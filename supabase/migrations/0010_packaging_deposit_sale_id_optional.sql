-- packaging_deposit_transactions.sale_id pasa a ser opcional: hasta ahora todo movimiento de
-- depósito debía venir de una venta (deposit_charged, ver NewSaleViewModel), pero el flujo de
-- "Devolver envase" (Ventas) registra un deposit_returned que no necesariamente ocurre junto a
-- una venta nueva — el cliente puede simplemente devolver el envase un día cualquiera.

alter table packaging_deposit_transactions
  alter column sale_id drop not null;
