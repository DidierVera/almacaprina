# Data Model — Goat Dairy Business App

Nota: los nombres de tablas, campos y valores de enum están en inglés (van directo al código). Las descripciones quedan en español para referencia del equipo.

Este modelo soporta desde el día 1 no solo venta de leche cruda, sino también productos derivados (queso, kéfir, mantequilla, etc.), con trazabilidad de costos por insumo — no solo un monto suelto por lote.

## Entity summary

1. Goat
2. WeightRecord
3. ReproductiveEvent
4. MilkProductionRecord
5. HealthRecord
6. FeedingRecord
7. Insumo
8. ProductRecipeItem
9. Product
10. Packaging
11. ProductionBatch
12. ProductionBatchInsumoUsage
13. Purchase
14. Customer
15. Sale
16. PackagingInventory
17. PackagingDepositTransaction
18. BusinessSettings
19. CareTask
20. CareTaskLog

---

## 1. Goat (entidad central)

| Field | Type | Descripción |
|---|---|---|
| id | UUID (PK) | Identificador único interno |
| tag_number | text | Número de arete/chip |
| name | text | Nombre del animal |
| photo_url | text | Foto de referencia |
| sex | enum (male, female) | |
| breed_composition | jsonb — array de `{breed_name: text, percentage: decimal}` | Puede ser 100% de una raza o una mezcla. Si `origin = born_on_farm` y se conocen ambos padres, se calcula automáticamente como el promedio de la composición de la madre y el padre (herencia 50/50) al crear la ficha del cabrito junto con el evento de parto; si `origin = purchased`, se captura manualmente. Siempre editable a mano después. Si el padre es semental externo (no está en el sistema), el cálculo solo refleja el aporte conocido de la madre — no se inventa un 50% para una raza desconocida. Ver `business/averageBreedComposition` |
| birth_date | date | |
| mother_id | UUID (FK → Goat) | Nula si es fundadora del hato o de origen externo |
| father_id | UUID (FK → Goat) | Nula si es semental externo |
| external_father_description | text | Si el padre no está en el sistema |
| current_status | enum (kid, young_doe, in_production, pregnant, dry, breeding_buck, retired, deceased) | **Siempre manual.** El admin lo define y actualiza a mano en cualquier momento — NO se deriva ni se actualiza automáticamente a partir de `ReproductiveEvent` (partos, montas, etc.). Corrección explícita confirmada con el dueño; cualquier versión anterior de este documento que diga lo contrario está desactualizada |
| current_weight_kg | decimal | Último peso registrado (espejo del último WeightRecord) |
| current_body_condition_score | integer (1-5) | Body Condition Score |
| herd_entry_date | date | Nacimiento o compra |
| weaning_date | date | Fecha en que la cría deja de tomar leche de la madre. Opcional — solo aplica a crías |
| origin | enum (born_on_farm, purchased) | |
| exit_date | date | Nula si sigue activa |
| exit_reason | text | Venta, muerte, descarte, etc. |
| notes | text | Observaciones generales |

**Índices sugeridos:** `tag_number` (único), `current_status` (para filtrar rápido el hato)

**Regla de alta manual:** si `origin = purchased` y la cabra tiene **6 meses o más** al momento del alta (`GOAT_ADULT_AGE_MONTHS = 6`, confirmado con el dueño), el formulario exige seleccionar `current_status` explícitamente. Para cabras nacidas en el sistema, el estado inicial es `kid` por defecto pero igual queda editable en cualquier momento.

**Índices sugeridos:** `tag_number` (único), `current_status` (para filtrar rápido el hato)

---

## 2. WeightRecord

| Field | Type | Descripción |
|---|---|---|
| id | UUID (PK) | |
| goat_id | UUID (FK → Goat) | |
| date | date | |
| weight_kg | decimal | |
| body_condition_score | integer (1-5) | |
| notes | text | |

Frecuencia esperada: cada 15 días.

---

## 3. ReproductiveEvent

| Field | Type | Descripción |
|---|---|---|
| id | UUID (PK) | |
| doe_id | UUID (FK → Goat) | Cabra hembra |
| event_type | enum (heat_detected, breeding, pregnancy_diagnosis, birth, abortion) | |
| date | date | |
| buck_id | UUID (FK → Goat) | Macho, nulo si no aplica |
| expected_birth_date | date (calculated) | = fecha de breeding + 150 días |
| result | enum (pending, successful, failed) | |
| kids_born_count | integer | Solo si event_type = birth |
| kids_alive_count | integer | |
| kid_ids | array de UUID (FK → Goat) | Vincula automáticamente con las fichas de los nuevos cabritos |
| notes | text | Complicaciones, asistencia veterinaria, etc. |

`current_status` de Goat **NO** se actualiza automáticamente por este evento — es siempre manual (ver regla en la entidad `Goat`). `expected_birth_date` sí es útil para las alertas de `UpcomingAlerts` independientemente del estado manual de la cabra.

---

## 4. MilkProductionRecord

Producción de leche cruda en el establo — la fuente de todo lo demás (venta directa o transformación en `ProductionBatch`).

| Field | Type | Descripción |
|---|---|---|
| id | UUID (PK) | |
| goat_id | UUID (FK → Goat) | |
| date | date | |
| morning_milking_liters | decimal | |
| evening_milking_liters | decimal | |
| total_liters_day | decimal (calculated) | |
| days_in_milk | integer (calculated) | Días desde el último birth |
| fat_pct | decimal | Opcional |
| protein_pct | decimal | Opcional |
| no_milking_reason | enum (dry, sick, under_treatment, other) | Nulo si sí se ordeñó |
| no_milking_reason_detail | text | Solo si no_milking_reason = other |

**Vista/tabla calculada:** `DailyHerdProduction` = suma de `total_liters_day` de todas las cabras activas, por fecha.

---

## 5. HealthRecord

| Field | Type | Descripción |
|---|---|---|
| id | UUID (PK) | |
| goat_id | UUID (FK → Goat), nullable | Nulo cuando el evento es a nivel de GRUPO — ej. una `CareTask` de tipo `medication` completada desde el checklist de Campo (`CareTask` es siempre de grupo, nunca de una cabra puntual). Los registros individuales (creados desde la ficha técnica) siempre lo llevan |
| type | enum (vaccine, deworming, treatment, routine_checkup, diagnosis) | |
| date | date | |
| description | text | |
| insumo_id | UUID (FK → Insumo) | El producto veterinario aplicado (vacuna, desparasitante, etc.). Nulo si el evento no involucra un insumo físico (ej. revisión de rutina) |
| dosage | text | Descripción de la dosis administrada (ej. "5 ml", "2 tabletas") |
| quantity_used | decimal | Cantidad consumida de `insumo_id`, en la unidad de `Insumo.unit_of_measure` — necesaria para descontar existencias con precisión (la dosis en texto es para el registro clínico, esta es para inventario) |
| milk_withdrawal_days | integer | Días en que la leche NO es vendible tras el tratamiento |
| cost | decimal | Costo de este evento puntual. Se suma por separado a `Purchase` en `CostPerLiter` — ver regla de no deduplicación en las notas técnicas |
| veterinarian | text | |
| next_suggested_date | date | Para generar alertas automáticas |

**Regla de resolución en el checklist diario:** un `HealthRecord` **individual** (`goat_id` no nulo) con `next_suggested_date ≤ hoy` aparece como tarjeta en `DailyCareChecklist` (ver esa vista). Se considera resuelto y deja de aparecer en cuanto existe **otro** `HealthRecord` posterior para la misma `goat_id` **del mismo `type`** (o mismo `insumo_id`, si aplica) — se asume que ese registro nuevo es el seguimiento. Al confirmar la tarjeta desde el checklist, la app **crea un `HealthRecord` nuevo** (la aplicación real de hoy, con su propio `insumo_id`/`cost`/`dosage`), no modifica el registro original — si hace falta otra dosis futura, ese nuevo registro lleva su propio `next_suggested_date`. Los `HealthRecord` de grupo (`goat_id` nulo) NO participan de este mecanismo de recordatorio — su ejecución se controla por `CareTask.frequency` + `CareTaskLog`, igual que `feeding`.

---

## 6. FeedingRecord

| Field | Type | Descripción |
|---|---|---|
| id | UUID (PK) | |
| date | date | |
| animal_group | enum (lactating, pregnant, young_does, dry, breeding_bucks, general) | |
| goat_id | UUID (FK → Goat) | Opcional, solo si el registro es individual |
| insumo_id | UUID (FK → Insumo) | El alimento usado (concentrado, forraje, sales minerales, etc.), normalmente con `Insumo.category = feed` |
| quantity | decimal | En la unidad de `Insumo.unit_of_measure` |
| cost | decimal | Costo de este registro puntual. Se suma por separado a `Purchase` en `CostPerLiter` — ver regla de no deduplicación en las notas técnicas |

---

## 7. Insumo

Catálogo de todo lo que se compra y se consume en la operación (no incluye envases, que viven en `Packaging` por su lógica de depósito).

| Field | Type | Descripción |
|---|---|---|
| id | UUID (PK) | |
| name | text | "Concentrado para cabras", "Cuajo líquido", "Sal", "Vacuna triple", "Combustible transporte" |
| category | enum (feed, veterinary, processing_input, transport, labor, maintenance, other) | `processing_input` es para insumos de transformación de leche (cuajo, sal, cultivos, etc.) |
| unit_of_measure | enum (kg, g, liter, ml, unit) | |
| last_unit_cost | decimal | Se actualiza automáticamente con cada `Purchase` de este insumo — sirve para prellenar costos en `FeedingRecord`, `HealthRecord` y `ProductionBatchInsumoUsage` antes de la próxima compra |
| reorder_lead_time_days | integer | Cuántos días antes de quedarse sin existencias se quiere la alerta (ej. 3). Se compara contra `InsumoDaysRemaining` |
| purchase_package_label | text | Cómo se compra normalmente (ej. "bulto de 40kg", "caja de 12 unidades") |
| purchase_package_size | decimal | Cantidad que trae ese empaque, en `unit_of_measure` |
| notes | text | |
| active | boolean | Para descontinuar sin borrar historial |

---

## 8. ProductRecipeItem

La "receta" estándar de un producto derivado: qué insumos y cuánto se espera usar por unidad producida. No incluye la leche en sí (esa se registra directo en `ProductionBatch.milk_liters_used`, porque es producción propia del hato, no un insumo comprado).

| Field | Type | Descripción |
|---|---|---|
| id | UUID (PK) | |
| product_id | UUID (FK → Product) | Debe tener `category = derived_dairy` |
| insumo_id | UUID (FK → Insumo) | |
| quantity_per_output_unit | decimal | Cantidad esperada de este insumo por cada unidad de `Product.sale_unit` producida (ej. 5 ml de cuajo por kg de feta) |

Se usa para prellenar `ProductionBatchInsumoUsage` automáticamente al registrar un lote nuevo — el usuario puede ajustar las cantidades reales si ese lote usó más o menos.

---

## 9. Product

Catálogo de todo lo que se puede vender: leche cruda y cualquier producto derivado futuro.

| Field | Type | Descripción |
|---|---|---|
| id | UUID (PK) | |
| name | text | "Leche fresca", "Queso feta", "Kéfir", "Mantequilla", etc. |
| category | enum (raw_milk, derived_dairy) | `raw_milk` identifica el producto que se descuenta directo de `DailyHerdProduction` |
| sale_unit | enum (liter, kilogram, unit) | |
| default_unit_price | decimal | Precio sugerido, editable por venta |
| active | boolean | Para "descontinuar" sin borrar historial |

**Dato semilla obligatorio:** debe existir un registro `Product` con `name = "Leche fresca"`, `category = raw_milk`, `sale_unit = liter`, `default_unit_price = 12000`.

---

## 10. Packaging

Catálogo de envases, desacoplado de cualquier producto específico y separado de `Insumo` por su lógica de depósito retornable.

| Field | Type | Descripción |
|---|---|---|
| id | UUID (PK) | |
| name | text | "Botella plástico 1L", "Botella vidrio 1L", "Empaque al vacío 500g" |
| is_returnable | boolean | |
| deposit_amount | decimal | Nulo si no aplica (envases no retornables) |
| unit_cost | decimal | Costo de producción/compra del envase |

**Dato semilla:** "Botella plástico 1L" (`is_returnable = false`, `unit_cost = 1200`) y "Botella vidrio 1L" (`is_returnable = true`, `deposit_amount = 4000`).

---

## 11. ProductionBatch

Registra la transformación de leche cruda en un producto derivado.

| Field | Type | Descripción |
|---|---|---|
| id | UUID (PK) | |
| date | date | |
| output_product_id | UUID (FK → Product) | Debe tener `category = derived_dairy` |
| milk_liters_used | decimal | Litros de leche cruda consumidos en este lote |
| output_quantity | decimal | Cantidad producida, en la unidad del producto (ej. kg) |
| yield_ratio | decimal (calculated) | = milk_liters_used / output_quantity. Para feta, debería salir ≈ 6-8 |
| responsible | text | Quién hizo el proceso |
| notes | text | |

El costo de insumos de este lote NO es un campo aquí — se calcula sumando `ProductionBatchInsumoUsage.cost` de todos los registros ligados a este lote (ver siguiente entidad).

Cada `ProductionBatch` **descuenta** `milk_liters_used` del inventario de leche cruda disponible (ver `RawMilkAvailableBalance`) y **aumenta** el inventario del producto derivado correspondiente.

---

## 12. ProductionBatchInsumoUsage

Consumo real de insumos en un lote específico — se prellena desde `ProductRecipeItem` al crear el lote, pero es editable.

| Field | Type | Descripción |
|---|---|---|
| id | UUID (PK) | |
| production_batch_id | UUID (FK → ProductionBatch) | |
| insumo_id | UUID (FK → Insumo) | |
| quantity_used | decimal | Cantidad real usada en este lote |
| unit_cost_at_time | decimal | Copia de `Insumo.last_unit_cost` al momento del lote — no cambia si el precio del insumo se actualiza después, para no distorsionar costos históricos |
| cost | decimal (calculated) | = quantity_used × unit_cost_at_time |

---

## 13. Purchase

| Field | Type | Descripción |
|---|---|---|
| id | UUID (PK) | |
| date | date | |
| category | enum (feed, veterinary, packaging, processing_input, transport, labor, maintenance, other) | Determina si se llena `insumo_id` o `packaging_id` (ver siguientes dos campos) |
| insumo_id | UUID (FK → Insumo) | Requerido si category ≠ packaging |
| packaging_id | UUID (FK → Packaging) | Requerido si category = packaging. Al guardar, genera automáticamente un movimiento en `PackagingInventory` (movement_type = purchase) |
| supplier | text | |
| quantity | decimal | |
| unit | text | Normalmente heredado de `Insumo.unit_of_measure` o de la unidad del envase, editable por si acaso |
| unit_cost | decimal | Al guardar, actualiza `Insumo.last_unit_cost` (si aplica) |
| total_cost | decimal (calculated) | |
| notes | text | Observaciones libres opcionales |

---

## 14. Customer

| Field | Type | Descripción |
|---|---|---|
| id | UUID (PK) | |
| name | text | |
| type | enum (individual, business) | |
| contact | text | |
| address | text | Opcional |
| notes | text | |

---

## 15. Sale

| Field | Type | Descripción |
|---|---|---|
| id | UUID (PK) | |
| date | date | |
| customer_id | UUID (FK → Customer) | |
| product_id | UUID (FK → Product) | Para ventas de leche, referencia el Product "Leche fresca" |
| quantity_sold | decimal | En la unidad definida por `Product.sale_unit` |
| unit_price | decimal | Prellenado desde `Product.default_unit_price`, editable |
| packaging_id | UUID (FK → Packaging) | Nulo si el producto se vende a granel sin envase trackeado |
| new_packaging_units_count | integer | Envases nuevos entregados en esta venta. Puede ser 0 aunque `packaging_id` sea retornable, si el cliente reutiliza envases que ya tenía en su poder — en ese caso no se cobra depósito nuevo |
| deposit_charged | decimal (calculated) | new_packaging_units_count × Packaging.deposit_amount |
| packaging_returned_count | integer | Envases vacíos que el cliente devuelve en esta misma visita (0 si no aplica) |
| deposit_refunded | decimal (calculated) | packaging_returned_count × Packaging.deposit_amount |
| total_value | decimal (calculated) | (quantity_sold × unit_price) + deposit_charged − deposit_refunded |
| payment_method | enum (cash, transfer, other) | Si payment_status = pending al crear la venta, se define después, al momento de marcarla pagada |
| payment_status | enum (paid, pending) | "pending" = fiado |
| paid_date | date | Nula hasta que se marque como pagada |
| notes | text | |

---

## 16. PackagingInventory

| Field | Type | Descripción |
|---|---|---|
| id | UUID (PK) | |
| date | date | |
| packaging_id | UUID (FK → Packaging) | |
| movement_type | enum (purchase, sale_use, waste) | |
| quantity | integer | |
| unit_cost | decimal | Por defecto `Packaging.unit_cost`, editable |

---

## 17. PackagingDepositTransaction

| Field | Type | Descripción |
|---|---|---|
| id | UUID (PK) | |
| customer_id | UUID (FK → Customer) | |
| sale_id | UUID (FK → Sale) | |
| packaging_id | UUID (FK → Packaging) | |
| date | date | |
| movement_type | enum (deposit_charged, deposit_returned) | |
| quantity | integer | |
| amount | decimal (calculated) | quantity × Packaging.deposit_amount |

---

## 18. BusinessSettings

| Field | Type | Descripción |
|---|---|---|
| id | UUID (PK) | Fila única (singleton) |
| farm_name | text | Nombre de la finca, mostrado en Ajustes |
| currency | text | Código de moneda, ej. "COP" |
| target_daily_liters_goal | decimal | Meta de producción diaria, hoy 85. Editable desde el dashboard Admin |
| deposit_alert_days | integer | Días que un depósito de envase puede quedar sin devolver antes de generar alerta (ej. 15) |
| updated_at | date | |

**IMPORTANTE — NO agregar un campo editable de "costo de leche por litro" aquí.** Esto se probó en un mockup y se descartó explícitamente: el dueño confirmó que el costo por litro debe salir de `CostPerLiter` (calculado desde `Purchase` + `HealthRecord.cost` + `FeedingRecord.cost`), nunca ser un número que se escribe a mano. En la pantalla de Ajustes, ese valor se muestra de **solo lectura**, no como un input editable.

---

## 19. CareTask

Plantilla de una tarea recurrente de cuidado del hato — define el calendario diario sin que nadie lo llene a mano cada día. La administra el rol Compras/Admin; la ejecuta el rol Campo.

| Field | Type | Descripción |
|---|---|---|
| id | UUID (PK) | |
| name | text | "Dar concentrado — mañana", "Dar forraje — tarde", "Aplicar desparasitante mensual" |
| task_type | enum (milking, feeding, medication, weighing, other) | `milking` y `weighing` son recordatorios hacia los flujos ya existentes de ordeño/pesada, no generan un registro propio |
| frequency | enum (daily, specific_days, weekly, one_time) | |
| animal_group | enum (lactating, pregnant, young_does, dry, breeding_bucks, general, all) | Opcional |
| insumo_id | UUID (FK → Insumo) | Opcional, solo si la tarea consume un insumo (feeding, medication) |
| quantity_per_occurrence | decimal | Cantidad esperada de `insumo_id` por ocurrencia, en la unidad de `Insumo.unit_of_measure` |
| time_of_day | enum (morning, afternoon, both, any) | |
| active | boolean | |

---

## 20. CareTaskLog

El registro de que una tarea se marcó como hecha en una fecha específica.

| Field | Type | Descripción |
|---|---|---|
| id | UUID (PK) | |
| care_task_id | UUID (FK → CareTask) | |
| date | date | |
| completed | boolean | |
| actual_quantity_used | decimal | Prellenado desde `quantity_per_occurrence`, editable si ese día se usó distinta cantidad |
| linked_record_id | UUID | ID del `FeedingRecord` o `HealthRecord` generado automáticamente al marcar la tarea como hecha (si task_type = feeding o medication) |
| notes | text | |

Al completar una tarea de tipo `feeding` o `medication`, la app genera automáticamente el `FeedingRecord`/`HealthRecord` correspondiente — `CareTask` es la plantilla, no una fuente de datos paralela de consumo.

---

## Relaciones

```
Goat (1) ──< (N) WeightRecord
Goat (1) ──< (N) ReproductiveEvent [as doe_id]
Goat (1) ──< (N) ReproductiveEvent [as buck_id]
Goat (1) ──< (N) MilkProductionRecord
Goat (1) ──< (N) HealthRecord
Goat (1) ──< (N) FeedingRecord [optional, individual]
Goat (mother/father) ──< (N) Goat [kids] (auto-referencia)

Insumo (1) ──< (N) Purchase [cuando category ≠ packaging]
Insumo (1) ──< (N) FeedingRecord
Insumo (1) ──< (N) HealthRecord
Insumo (1) ──< (N) ProductRecipeItem
Insumo (1) ──< (N) ProductionBatchInsumoUsage

Product (1) ──< (N) Sale
Product (1) ──< (N) ProductionBatch [as output_product_id]
Product (1) ──< (N) ProductRecipeItem

Packaging (1) ──< (N) Sale
Packaging (1) ──< (N) PackagingInventory
Packaging (1) ──< (N) PackagingDepositTransaction
Packaging (1) ──< (N) Purchase [cuando category = packaging]

ProductionBatch (1) ──< (N) ProductionBatchInsumoUsage

Customer (1) ──< (N) Sale
Customer (1) ──< (N) PackagingDepositTransaction
Sale (1) ──< (N) PackagingDepositTransaction

Insumo (1) ──< (N) CareTask
CareTask (1) ──< (N) CareTaskLog
```

---

## Vistas calculadas

**Estado real de implementación (confirmado por inventario de Claude Code):**

| Vista | Cálculo | Uso | Estado |
|---|---|---|---|
| `DailyHerdProduction` | Σ total_liters_day por fecha | Producción total de leche cruda del día | ✅ Implementada |
| `RawMilkAvailableBalance` | Σ DailyHerdProduction − Σ Sale.quantity_sold (producto = leche) − Σ ProductionBatch.milk_liters_used | Litros de leche cruda realmente disponibles | ✅ Implementada |
| `DerivedProductInventory` | Σ ProductionBatch.output_quantity − Σ Sale.quantity_sold, por producto derivado | Stock de queso/kéfir/mantequilla | ❌ Pendiente |
| `ProductYieldRatio` | Promedio histórico de `yield_ratio` por producto derivado | Litros de leche reales necesarios por unidad de cada producto | ✅ Implementada (usada en Nuevo lote) |
| `ProductCostPerUnit` | ((milk_liters_used × CostPerLiter del día) + Σ ProductionBatchInsumoUsage.cost del lote) / output_quantity | Costo real de producir una unidad | ❌ Pendiente |
| `CurrentHerdStatus` | Conteo de cabras por `current_status` | Dashboard principal | ✅ Implementada |
| `CostPerLiter` | Σ costos (Purchase + HealthRecord.cost + FeedingRecord.cost) / litros producidos, por período | Costo de producir un litro de leche cruda | ✅ Implementada (solo lectura en Ajustes) |
| `FutureProductionProjection` | Basado en `expected_birth_date` + curva de lactancia estándar | Planeación de la meta de 85 L/día | ❌ Pendiente |
| `UpcomingAlerts` | Vacunas/desparasitaciones próximas + pesadas quincenales vencidas + apareamientos proyectados + insumos por agotarse + depósitos envejecidos | Notificaciones | ⚠️ Parcial — hoy son piezas sueltas combinadas ad-hoc en `AdminHomeViewModel`, no una función única. Si se refactoriza, mantener el mismo resultado visual ya validado en el mockup |
| `CustomerPackagingDepositBalance` | Σ deposit_charged − Σ deposit_returned, por cliente | Envases retornables que debe cada cliente | ❌ Pendiente — **necesaria para el módulo Ventas** |
| `TotalPackagingDepositsOut` | Σ de `CustomerPackagingDepositBalance` de todos los clientes | Contador global para el Home de Ventas | ❌ Pendiente — **necesaria para el módulo Ventas** |
| `AgedPackagingDeposits` | PackagingDepositTransaction (deposit_charged, sin deposit_returned correspondiente) con antigüedad ≥ `BusinessSettings.deposit_alert_days` | Alerta "Depósitos sin devolver" | ❌ Pendiente |
| `PendingSalesBalance` | Σ Sale.total_value donde payment_status = pending, por cliente | Cartera pendiente de cobro (fiado) | ❌ Pendiente — **necesaria para el módulo Ventas** |
| `NetMargin` | Σ Sale.total_value − Σ Purchase.total_cost (período) | Rentabilidad del negocio | ❌ Pendiente |
| `LactationNumber` | Conteo de ReproductiveEvent (event_type=birth, result=successful) de esa cabra hasta la fecha | El "2ª lactancia" mostrado en ficha técnica y ordeño | ❌ Pendiente |
| `InsumoStockBalance` | Σ Purchase.quantity − Σ FeedingRecord.quantity − Σ HealthRecord.quantity_used − Σ ProductionBatchInsumoUsage.quantity_used, por insumo | Existencias reales de cada insumo | ❌ Pendiente |
| `InsumoPlannedDailyConsumption` | Σ CareTask.quantity_per_occurrence de tareas activas diarias, por insumo | Cuánto se espera consumir por día | ❌ Pendiente |
| `InsumoDaysRemaining` | InsumoStockBalance ÷ InsumoPlannedDailyConsumption, por insumo | Días de existencias al ritmo actual de consumo | ❌ Pendiente (el mockup de alertas la muestra, pero es simulada/estática todavía) |
| `DailyCareChecklist` | Unión de dos fuentes: (1) CareTask activos aplicables hoy, cruzados con CareTaskLog del día; (2) HealthRecord individuales con `next_suggested_date` ≤ hoy sin seguimiento posterior registrado (ver regla de resolución bajo `HealthRecord`) | El checklist diario que ve Campo — tareas de grupo Y seguimientos de salud puntuales a una cabra específica | ❌ Pendiente — **es la pieza central del módulo Campo que sigue** |

---

## Convención de nombres

- Tablas: `PascalCase` singular en el modelo de dominio (Kotlin data class); si se mapean a tablas SQL en Supabase, usar `snake_case` plural (ej. `Goat` → tabla `goats`, `ProductionBatchInsumoUsage` → tabla `production_batch_insumo_usages`)
- Campos: `snake_case` en inglés, tanto en Kotlin (donde se puede usar camelCase idiomático) como en la base de datos
- Enums: valores en inglés, minúsculas, `snake_case` (ej. `in_production`, `processing_input`)
