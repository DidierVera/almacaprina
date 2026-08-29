# Data Structure — Goat Milk Business Management App

## Entity summary

1. Goat
2. WeightRecord
3. ReproductiveEvent
4. MilkProductionRecord
5. HealthRecord
6. FeedingRecord
7. Supply / Purchase
8. Customer
9. Sale
10. BottleInventory

---

## 1. Goat (central entity)

| Field | Type | Description |
|---|---|---|
| id | UUID (PK) | Internal unique identifier |
| tag_number | text | Ear tag / chip number |
| name | text | Animal's name |
| photo_url | text | Reference photo |
| sex | enum (male, female) | |
| breed | text | Main breed |
| breed_crosses | text/array | Cross details if mixed-breed |
| birth_date | date | |
| mother_id | UUID (FK → Goat) | Null if she's a herd founder or of external origin |
| father_id | UUID (FK → Goat) | Null if the sire is external (in that case use `external_father_description`) |
| external_father_description | text | If the father isn't in the system |
| current_status | enum (kid, doeling, in_production, pregnant, dry, breeding_buck, retired, deceased) | Calculated or updated by events |
| current_weight_kg | decimal | Latest recorded weight (mirror of the last WeightRecord) |
| current_body_condition | integer (1-5) | Body Condition Score |
| herd_entry_date | date | Birth or purchase |
| origin | enum (born_on_farm, purchased) | |
| exit_date | date | Null if still active |
| exit_reason | text | Sale, death, cull, etc. |
| notes | text | General observations |

**Suggested indexes:** `tag_number` (unique), `current_status` (for quickly filtering the herd)

---

## 2. WeightRecord

| Field | Type | Description |
|---|---|---|
| id | UUID (PK) | |
| goat_id | UUID (FK → Goat) | |
| date | date | |
| weight_kg | decimal | |
| body_condition | integer (1-5) | |
| observations | text | |

Expected frequency: every 15 days, as already defined.

---

## 3. ReproductiveEvent

| Field | Type | Description |
|---|---|---|
| id | UUID (PK) | |
| doe_id | UUID (FK → Goat) | |
| event_type | enum (heat_detected, breeding, pregnancy_diagnosis, kidding, abortion) | |
| date | date | |
| buck_id | UUID (FK → Goat) | Null if not applicable |
| expected_kidding_date | date (calculated) | = breeding date + 150 days |
| result | enum (pending, successful, failed) | |
| kids_born_count | integer | Only if event_type = kidding |
| kids_alive_count | integer | |
| kid_ids | array of UUID (FK → Goat) | Automatically links to the new kids' records |
| observations | text | Complications, veterinary assistance, etc. |

**Note:** this is the heart of the reproductive module. A Goat's `current_status` should update automatically based on its latest event (e.g.: breeding recorded → status "pregnant" until the kidding is recorded).

---

## 4. MilkProductionRecord

| Field | Type | Description |
|---|---|---|
| id | UUID (PK) | |
| goat_id | UUID (FK → Goat) | |
| date | date | |
| morning_milking_liters | decimal | |
| evening_milking_liters | decimal | |
| total_daily_liters | decimal (calculated) | |
| days_in_lactation | integer (calculated) | Days since the last kidding |
| fat_pct | decimal | Optional, if measuring quality |
| protein_pct | decimal | Optional |

**Useful calculated view/table:** `DailyHerdProduction` = sum of `total_daily_liters` for all active goats, by date. This is what gives the "10 L/day" figure or progress toward the "85 L/day" goal.

---

## 5. HealthRecord

| Field | Type | Description |
|---|---|---|
| id | UUID (PK) | |
| goat_id | UUID (FK → Goat) | |
| type | enum (vaccination, deworming, treatment, routine_checkup, diagnosis) | |
| date | date | |
| description | text | |
| product_applied | text | |
| dosage | text | |
| milk_withdrawal_days | integer | Days during which the milk is NOT sellable after treatment |
| cost | decimal | |
| responsible_veterinarian | text | |
| next_suggested_date | date | For generating automatic alerts |

---

## 6. FeedingRecord

| Field | Type | Description |
|---|---|---|
| id | UUID (PK) | |
| date | date | |
| animal_group | enum (lactating, pregnant, doelings, dry, breeders, general) | Managed by group, not by individual, unless fine-grained detail is needed |
| goat_id | UUID (FK → Goat) | Optional, only if the record is for an individual |
| feed_type | text | Concentrate, forage, mineral salts, etc. |
| quantity_kg | decimal | |
| cost | decimal | |

---

## 7. Supply / Purchase

| Field | Type | Description |
|---|---|---|
| id | UUID (PK) | |
| date | date | |
| category | enum (feed, veterinary, packaging, transport, labor, maintenance, other) | |
| description | text | |
| supplier | text | |
| quantity | decimal | |
| unit | text | kg, liters, units, etc. |
| unit_cost | decimal | |
| total_cost | decimal (calculated) | |

This table is what lets you calculate **actual cost per liter produced** by cross-referencing it with `DailyHerdProduction`.

---

## 8. Customer

| Field | Type | Description |
|---|---|---|
| id | UUID (PK) | |
| name | text | |
| type | enum (individual, business) | |
| contact | text | Phone/WhatsApp |
| address | text | Optional, for deliveries |
| notes | text | |

---

## 9. Sale

| Field | Type | Description |
|---|---|---|
| id | UUID (PK) | |
| date | date | |
| customer_id | UUID (FK → Customer) | |
| liters_sold | decimal | |
| unit_price | decimal | Defaults to $12,000 COP, but editable in case of negotiation |
| total_value | decimal (calculated) | |
| payment_method | enum (cash, transfer, other) | |
| notes | text | |

---

## 10. BottleInventory

| Field | Type | Description |
|---|---|---|
| id | UUID (PK) | |
| date | date | |
| movement_type | enum (purchase, used_in_sale, waste) | |
| quantity | integer | |
| unit_cost | decimal | Defaults to $1,200 COP |

---

## Relationships between entities

```
Goat (1) ──< (N) WeightRecord
Goat (1) ──< (N) ReproductiveEvent [as doe]
Goat (1) ──< (N) ReproductiveEvent [as buck/sire]
Goat (1) ──< (N) MilkProductionRecord
Goat (1) ──< (N) HealthRecord
Goat (1) ──< (N) FeedingRecord [optional, individual]
Goat (mother/father) ──< (N) Goat [offspring] (self-reference)

Customer (1) ──< (N) Sale
```

---

## Calculated tables / suggested views (not manually captured, derived)

| View | Calculation | Use |
|---|---|---|
| `DailyHerdProduction` | Σ total_daily_liters by date | Tracking the 85 L/day goal |
| `CurrentHerdStatus` | Count of goats by `current_status` | Main dashboard |
| `CostPerLiter` | Σ costs (supplies+health+feeding) / liters produced, per period | Actual profitability |
| `FutureProductionProjection` | Based on `expected_kidding_date` + standard lactation curve | Planning toward the 85 L/day goal |
| `UpcomingAlerts` | Upcoming vaccinations/dewormings (HealthRecord.next_suggested_date) + overdue biweekly weigh-ins + projected breedings | Notifications |
| `NetMargin` | Σ Sale.total_value − Σ Supply.total_cost (period) | Business profitability |

---

## Implementation notes

- **Suggested engine:** a relational database (PostgreSQL or SQLite) fits very well here because there are strong relationships (goat–mother–father, goat–events). If you prefer something simpler to start, SQLite is enough and needs no server.
- **Enums as catalog tables:** if you think you'll need to add breeds, supply categories, etc. frequently, consider managing them as independent tables instead of fixed enums in code.
- **Auditing:** it's worth adding `created_at` and `updated_at` to every table, for traceability.
