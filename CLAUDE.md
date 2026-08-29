# Almacaprina — Goat Milk Business Management App

## Business context

App to manage a goat dairy farm. Production goal: 85 L/day.
Current herd status (reference, changes over time — see the database for the real state):
- ~19 adult goats: pregnant, in production, and young doelings (4-6 months)
- 2 kids
- Optimal production per goat: 2-4 L/day; bringing a goat to optimal condition takes ~1 month
- Sale price: $12,000 COP/liter
- Bottle cost (bottle + cap + label): $1,200 COP/unit

The app is used by 3 people with distinct roles:
- **Field**: records daily milking and biweekly weigh-ins
- **Sales**: records sales and customers
- **Purchases/Admin** (owner): supply purchases, full access

## Tech stack (decisions already made — don't change them without confirming)

- **Kotlin Multiplatform**: business logic shared between Android and iOS
- **Android**: native with Jetpack Compose
- **iOS**: native with SwiftUI
- **Backend**: Supabase (Postgres + Auth + Row Level Security + Storage/Realtime)
  - Chosen over Firebase because the data model is relational (see schema below)
  - RLS to separate permissions by role (field / sales / admin)
  - KMP client: `supabase-kt`
- **Future local cache (if offline support is needed)**: SQLDelight

## Module structure

```
shared/
  src/commonMain/kotlin/com/almacaprina/
    domain/model/         # Goat, Sale, ReproductiveEvent, etc.
    domain/repository/    # Data access interfaces
    data/remote/          # supabase-kt client
    data/mapper/          # Supabase <-> domain model conversion
    business/             # Pure logic: days in lactation, cost/liter, projections
  src/androidMain/kotlin/com/almacaprina/   # current/ implementations
  src/iosMain/kotlin/com/almacaprina/       # current/ implementations
androidApp/   # Jetpack Compose UI
iosApp/       # SwiftUI UI
```

## Data model

See `docs/business_data_structure.md` for the full schema of entities, fields, and relationships (Goat, WeightRecord, ReproductiveEvent, MilkProductionRecord, HealthRecord, FeedingRecord, Supply/Purchase, Customer, Sale, BottleInventory).

Key points to respect in the code:
- `Goat` has a self-reference to `mother_id` / `father_id`
- A Goat's `current_status` is a field derived from its reproductive events (don't edit manually without going through the business logic)
- Goat gestation ≈ 150 days — use this constant for `expected_kidding_date`
- Important calculated views: `DailyHerdProduction`, `CostPerLiter`, `FutureProductionProjection`

## Conventions

- Table and field names in English, in `snake_case`, consistent with the schema in `docs/`
- All business calculations (liters, costs, projections) live in `shared/business/`, never duplicated in Android or iOS
- Before creating a new table or field, check whether it already exists in `docs/business_data_structure.md`

## Next pending step

The SQL table-creation script and the role-based RLS policies are already in `supabase/migrations/`. Still pending: apply the migration to the Supabase project and wire up the `supabase-kt` client from `shared/data/remote/`.
