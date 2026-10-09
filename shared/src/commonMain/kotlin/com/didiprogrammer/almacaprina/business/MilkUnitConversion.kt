package com.didiprogrammer.almacaprina.business

/** Unidades en las que Campo puede capturar una cantidad de leche; siempre se guarda en ml. */
enum class MilkEntryUnit { MILLILITER, OUNCE }

/** Mililitros por litro — para convertir los ml guardados a los litros que usan los demás cálculos. */
const val MILLILITERS_PER_LITER = 1000.0

/** Densidad de la leche de cabra en g/ml (típicamente 1,030–1,034) — la leche NO pesa lo mismo que el agua. */
const val GOAT_MILK_DENSITY_G_PER_ML = 1.03

/** Gramos por onza de peso (avoirdupois) — la unidad de la báscula, no la onza fluida. */
const val GRAMS_PER_OUNCE = 28.3495

/** ml: se toma tal cual. oz: onzas de peso → gramos → ml dividiendo entre la densidad de la leche. */
fun MilkEntryUnit.toMilliliters(value: Double): Double = when (this) {
    MilkEntryUnit.MILLILITER -> value
    MilkEntryUnit.OUNCE -> value * GRAMS_PER_OUNCE / GOAT_MILK_DENSITY_G_PER_ML
}

/** Inversa de [toMilliliters] — para mostrar un registro ya guardado (en ml) en la unidad elegida. */
fun MilkEntryUnit.fromMilliliters(ml: Double): Double = when (this) {
    MilkEntryUnit.MILLILITER -> ml
    MilkEntryUnit.OUNCE -> ml * GOAT_MILK_DENSITY_G_PER_ML / GRAMS_PER_OUNCE
}
