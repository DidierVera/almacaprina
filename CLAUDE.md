# Almacaprina — App de gestión de negocio de leche de cabra

## Contexto de negocio

App para administrar una finca productora de leche de cabra. Objetivo de producción: 85 L/día (configurable, ver `BusinessSettings`).
Estado actual del hato (referencia, cambia con el tiempo — ver base de datos para el estado real):
- ~19 cabras adultas: gestantes, en producción, y cabretonas jóvenes (4-6 meses)
- 2 cabritos
- Producción óptima por cabra: 2-4 L/día; llevar una cabra a condición óptima toma ~1 mes
- Precio de venta de la leche: $12.000 COP/litro
- Costo de envase plástico: $1.200 COP/unidad. Envase de vidrio: retornable, depósito de $4.000 COP, devuelto completo al retornar el envase

**Visión a futuro (ya contemplada en el modelo de datos):** el negocio puede vender productos derivados de la leche (queso, kéfir, mantequilla). Cada derivado tiene una "receta" de insumos (`ProductRecipeItem`) y su costo real se calcula por insumo consumido, no como un número suelto. No asumir en el código que el negocio solo vende leche cruda, incluso si por ahora es lo único que se vende.

La app la usan 3 personas con roles distintos:
- **Campo**: registra ordeño diario y pesadas quincenales
- **Ventas**: registra ventas, clientes, y cobro de pendientes (fiado)
- **Compras/Admin** (dueño): compras de insumos, gestión del catálogo de productos/envases/insumos, recetas por producto, registro de lotes de producción, acceso completo

## Stack técnico (decisiones ya tomadas — no las cambies sin confirmar)

- **Kotlin Multiplatform + Compose Multiplatform**: UI compartida entre Android e iOS — un solo código de pantallas, no implementaciones nativas separadas. Generado desde el wizard de kmp.jetbrains.com (Gradle, Android con Compose, iOS compartiendo UI con Compose Multiplatform)
- **Android**: ejecuta la UI compartida vía `composeApp`, con una `MainActivity` mínima como punto de entrada
- **iOS**: proyecto Xcode mínimo (`iosApp`) que aloja la UI compartida a través de un `UIViewController` — la mayoría del código Swift es solo el bootstrap, no pantallas propias
- **Backend**: Supabase (Postgres + Auth + Row Level Security + Storage/Realtime)
  - Elegido sobre Firebase porque el modelo de datos es relacional
  - RLS para separar permisos por rol (campo / ventas / admin)
  - Cliente KMP: `supabase-kt`
- **Caché local futura (si se necesita offline)**: SQLDelight

## Autenticación

- Login principal: correo + contraseña vía Supabase Auth
- Cada uno de los 3 usuarios (Campo, Ventas, Compras/Admin) tiene su propia cuenta y su propio dispositivo — no hay selección de perfil en un dispositivo compartido
- El rol del usuario determina la pantalla de destino tras el login
- **PIN de acceso rápido (opcional, pensado sobre todo para el rol Campo)**:
  - Se ofrece crear un PIN de 4 dígitos justo después del primer login exitoso; se puede omitir
  - El PIN es **solo un candado local del dispositivo** (almacenado hasheado en almacenamiento seguro local — Keystore en Android, Keychain en iOS), NO es un mecanismo de autenticación contra Supabase
  - El PIN únicamente desbloquea la UI mientras la sesión real de Supabase siga vigente. Si la sesión expiró, el PIN no sirve — se debe forzar login completo con contraseña
  - Después de varios intentos fallidos de PIN (definir constante, ej. 5), forzar login completo con contraseña
  - No implementar el PIN como si fuera equivalente a autenticación real

## Estructura de módulos

**Nombres reales del proyecto (plantilla clásica de kmp.jetbrains.com, no la de módulo unificado `composeApp`):**

```
shared/
  src/commonMain/kotlin/com/didiprogrammer/almacaprina/
    ui/                    # Pantallas Compose compartidas (screens, componentes, navegación) — Android e iOS usan el mismo código
    domain/model/          # Goat, Insumo, Product, ProductionBatch, Sale, etc.
    domain/repository/     # Interfaces de acceso a datos
    data/remote/           # Cliente supabase-kt
    data/mapper/           # Conversión Supabase <-> modelos de dominio
    business/              # Lógica pura: días en lactancia, costo/litro, proyecciones, yield ratio
  src/androidMain/kotlin/com/didiprogrammer/almacaprina/   # implementaciones actual/ (ej. Keystore para el PIN)
  src/iosMain/kotlin/com/didiprogrammer/almacaprina/       # implementaciones actual/ (ej. Keychain para el PIN)
androidApp/   # Módulo delgado: solo MainActivity, que arranca la UI compartida de `shared`
iosApp/       # Proyecto Xcode delgado que aloja la misma UI compartida vía UIViewController
```

**No crear pantallas Swift en `iosApp` ni código de UI en `androidApp`** — cualquier pantalla nueva va en `shared/src/commonMain/kotlin/.../ui/`, compartida por definición. Las excepciones son solo integraciones de bajo nivel con el sistema operativo (ej. almacenamiento seguro del PIN, cámara), que sí van en `androidMain`/`iosMain` vía `expect`/`actual`.

## Modelo de datos

**Todo el código (nombres de tablas, campos, enums, clases) va en inglés.** Ver `docs/data_model.md` para el esquema completo de 18 entidades y sus relaciones.

La interfaz visual (textos que ve el usuario) sigue en español — la convención en inglés aplica solo al código y a la base de datos.

Puntos clave a respetar en el código:

**Hato y producción de leche:**
- `Goat` tiene auto-referencia a `mother_id` / `father_id`
- `current_status` se deriva automáticamente de eventos reproductivos, EXCEPTO en altas manuales de cabras adultas sin historial (ej. compradas): ahí el formulario de alta pide el estado inicial como campo explícito
- Gestación de cabra ≈ 150 días — usar esta constante para `expected_birth_date`
- `MilkProductionRecord` tiene `no_milking_reason` (enum: dry, sick, under_treatment, other) confirmado con el dueño — no inventar otras categorías sin confirmar
- `LactationNumber` ("2ª lactancia" en la UI) no es un campo guardado — se calcula contando partos exitosos previos de esa cabra

**Insumos y costos:**
- `Insumo` es el catálogo único para todo lo que se compra y consume (alimento, veterinario, insumos de transformación, etc.) — EXCEPTO envases, que viven en `Packaging` por su lógica de depósito retornable
- `FeedingRecord.insumo_id` y `HealthRecord.insumo_id` referencian este catálogo — no usar texto libre para nombrar alimentos o productos veterinarios, siempre debe ser una fila de `Insumo`
- Al guardar una `Purchase`, actualizar `Insumo.last_unit_cost` (o el costo del `Packaging` si category = packaging) automáticamente
- **Confirmado con el dueño**: `Purchase.total_cost`, `HealthRecord.cost` y `FeedingRecord.cost` se suman TODOS por separado en `CostPerLiter`, sin deduplicación automática — es responsabilidad del usuario no registrar el mismo gasto en dos lados. No agregar lógica de "detección de duplicados" sin que el dueño lo pida explícitamente
- Cuando `Purchase.category = packaging`, genera automáticamente un movimiento en `PackagingInventory` usando el `packaging_id` seleccionado

**Productos derivados y producción:**
- `Sale` está generalizada por `product_id`, no asume leche. Para ventas de leche, referencia el `Product` semilla "Leche fresca". No hardcodear "litros" ni $12.000 en la lógica de ventas — deben salir de `Product`
- Los envases (`Packaging`) están desacoplados de los productos — no asumir que un envase pertenece a un solo producto
- El precio del litro de leche es siempre $12.000 sin importar el tipo de envase — el depósito es un cargo aparte
- El dinero del depósito de envase retornable NO es ingreso del negocio hasta que se devuelve — no mezclar `deposit_charged`/`deposit_refunded` con ingresos por venta
- **Confirmado con el dueño**: una venta puede tener `packaging_id` retornable con `new_packaging_units_count = 0` si el cliente reutiliza envases que ya tenía — no se cobra depósito adicional en ese caso
- `ProductRecipeItem` define la receta esperada de insumos por unidad de producto derivado (NO incluye leche, esa se registra directo en `ProductionBatch.milk_liters_used`)
- Al crear un `ProductionBatch`, prellenar `ProductionBatchInsumoUsage` desde `ProductRecipeItem × output_quantity`, pero permitir editar las cantidades reales antes de guardar
- El costo de insumos de un lote NUNCA es un campo suelto — siempre se calcula sumando `ProductionBatchInsumoUsage.cost` de ese lote
- `ProductionBatch` descuenta litros de `RawMilkAvailableBalance` y aumenta el stock del producto derivado correspondiente

**Ventas y cobros:**
- Las botellas siempre son de 1 litro para el producto Leche fresca (específico de ese producto, no una regla general)
- Existen ventas a crédito ("a veces") — `Sale.payment_status` puede quedar en `pending`
- Pantalla "Cobrar pendientes" (rol Ventas): V1 no soporta pagos parciales — una venta se paga completa o sigue pendiente

**Vistas calculadas importantes:** `DailyHerdProduction`, `RawMilkAvailableBalance`, `DerivedProductInventory`, `ProductYieldRatio`, `ProductCostPerUnit`, `CostPerLiter`, `FutureProductionProjection`, `LactationNumber`, `InsumoStockBalance`, `InsumoDaysRemaining`

**Calendario de tareas y alertas de insumos:**
- `CareTask` es una plantilla de tarea recurrente (dar concentrado, dar forraje, aplicar medicamento), la configura Admin
- `CareTaskLog` es la ejecución diaria — la marca Campo desde un checklist generado automáticamente (`DailyCareChecklist`), no una pantalla que se llena manualmente cada día
- Al completar una `CareTask` de tipo `feeding` o `medication`, generar automáticamente el `FeedingRecord`/`HealthRecord` correspondiente — `CareTask` NUNCA debe ser una fuente de datos de consumo paralela a esas entidades, es solo la plantilla y el recordatorio
- Las tareas de tipo `milking` y `weighing` son recordatorios hacia los flujos ya existentes de ordeño y pesada, no generan su propio registro
- **`CareTask` es solo para tareas recurrentes a nivel de grupo de animales** (ej. dar concentrado a las lactantes todos los días). Un tratamiento puntual a una sola cabra (ej. una cita veterinaria única) se registra directo en `HealthRecord` desde la ficha técnica de esa cabra — no crear una `CareTask` para eso, evita mezclar dos casos de uso distintos en la misma entidad
- El Home del rol Campo es un checklist diario (`DailyCareChecklist`), no solo la pantalla de ordeño — ordeño es la tarea destacada dentro de ese checklist, junto con las demás tareas generadas desde `CareTask` activas
- `InsumoStockBalance` se calcula solo (compras − consumo registrado en FeedingRecord/HealthRecord/ProductionBatchInsumoUsage), nunca se captura manualmente
- La alerta de insumo por agotarse compara `InsumoDaysRemaining` (existencias ÷ consumo diario planeado según `CareTask` activos) contra `Insumo.reorder_lead_time_days` — es una proyección basada en el calendario configurado, no un umbral fijo de cantidad

**Configuración:**
- La meta de producción diaria (85 L/día) NO debe estar hardcodeada — vive en `BusinessSettings.target_daily_liters_goal`, editable desde el dashboard Admin
- **`BusinessSettings` también incluye `farm_name`, `currency`, y `deposit_alert_days`** (umbral de días para alertar depósitos de envase sin devolver)
- **Confirmado explícitamente con el dueño tras revisar un mockup**: el costo de leche por litro usado en `ProductCostPerUnit` y mostrado en Ajustes DEBE salir de `CostPerLiter` (calculado desde `Purchase` + `HealthRecord.cost` + `FeedingRecord.cost`). NUNCA implementar esto como un campo editable en `BusinessSettings` — en la pantalla de Ajustes se muestra de solo lectura. Un mockup previo lo mostró como editable por error; quedó descartado explícitamente

**Pendientes de verificar en la implementación (detectados en revisión de mockups):**
- El formulario de "Nueva cabra" debe mostrar el campo "Estado inicial" SOLO cuando `origin = purchased` y la cabra es adulta (ver regla ya documentada arriba) — confirmar que el mockup final lo incluye, no se alcanzó a ver en la captura
- El formulario de "Nueva compra" debe cambiar el selector de "Insumo del catálogo" a "Envase del catálogo" (referenciando `Packaging`, no `Insumo`) cuando `category = packaging` — confirmar que esto ocurre en la implementación
- Usar el nombre "Leche fresca" consistentemente para el producto semilla de leche cruda (un mockup lo mostró como "Leche cruda" — alinear con `docs/data_model.md`)

## Convenciones

- Todo el código (clases, campos, tablas SQL, enums) en inglés, `snake_case` para DB y campos, `PascalCase` para clases/tablas de dominio — ver el detalle en `docs/data_model.md`
- Textos de interfaz de usuario (labels, botones, mensajes) en español
- Todo cálculo de negocio (litros, costos, proyecciones, yield ratio) vive en `shared/business/`, nunca duplicado en Android o iOS
- Antes de crear una tabla o campo nuevo, revisar si ya existe en `docs/data_model.md`

## Asignación de módulos por rol

- **Campo**: Registrar ordeño, registrar pesadas quincenales, checklist diario de tareas (`DailyCareChecklist`: dar concentrado, dar forraje, medicamentos programados)
- **Ventas**: Nueva venta, Cobrar pendientes
- **Compras/Admin**:
  - Vista general (producción vs. meta, estado del hato, alertas — incluye insumos por agotarse, resumen financiero)
  - Fichas técnicas del hato (crear, editar, consultar historial completo por cabra)
  - Catálogo de productos, envases e insumos (CRUD de `Product`, `Packaging`, `Insumo`, `ProductRecipeItem`)
  - Configuración del calendario de tareas recurrentes (`CareTask`)
  - Registro de lotes de producción (`ProductionBatch` + `ProductionBatchInsumoUsage`)
  - Compra de insumos (`Purchase`)
