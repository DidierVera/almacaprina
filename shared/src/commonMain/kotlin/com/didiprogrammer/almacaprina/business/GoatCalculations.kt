package com.didiprogrammer.almacaprina.business

import com.didiprogrammer.almacaprina.domain.model.BreedPercentage
import com.didiprogrammer.almacaprina.domain.model.Goat
import com.didiprogrammer.almacaprina.domain.model.GoatStatus
import com.didiprogrammer.almacaprina.domain.model.ReproductiveEvent
import com.didiprogrammer.almacaprina.domain.model.ReproductiveEventResult
import com.didiprogrammer.almacaprina.domain.model.ReproductiveEventType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.periodUntil

/**
 * Edad mínima (en meses) a partir de la cual una cabra recién dada de alta se considera
 * "adulta" para efectos del formulario de alta manual (ver CLAUDE.md § altas manuales de
 * cabras adultas sin historial). NO confirmado explícitamente con el dueño — se usa el
 * límite superior de la ventana de cabretona (4-6 meses) documentada en CLAUDE.md como
 * aproximación razonable. Ajustar si el dueño da un número exacto.
 */
const val GOAT_ADULT_AGE_MONTHS = 6

/** Edad en meses completos entre birthDate y una fecha de referencia. */
fun ageInMonths(birthDate: LocalDate, today: LocalDate): Int = birthDate.monthsUntil(today)

/**
 * Si la cabra debe tratarse como "adulta" en el alta manual. Si no se conoce la fecha de
 * nacimiento (común en compras de animales adultos), se asume adulta por seguridad —
 * es preferible preguntar el estado inicial de más a que falte un dato requerido.
 */
fun isLikelyAdult(birthDate: LocalDate?, today: LocalDate): Boolean =
    birthDate == null || ageInMonths(birthDate, today) >= GOAT_ADULT_AGE_MONTHS

/** Etiqueta legible de edad para el header de la ficha técnica (ej. "1 año 3 m", "5 meses"). */
fun ageLabel(birthDate: LocalDate, today: LocalDate): String {
    val period = birthDate.periodUntil(today)
    return when {
        period.years > 0 && period.months > 0 -> "${period.years} a ${period.months} m"
        period.years > 0 -> "${period.years} ${if (period.years == 1) "año" else "años"}"
        period.months > 0 -> "${period.months} ${if (period.months == 1) "mes" else "meses"}"
        else -> "${period.days} ${if (period.days == 1) "día" else "días"}"
    }
}

/** LactationNumber — conteo de partos exitosos previos de esa cabra hasta la fecha. */
fun lactationNumber(goatId: String, reproductiveEvents: List<ReproductiveEvent>): Int =
    reproductiveEvents.count {
        it.doeId == goatId &&
            it.eventType == ReproductiveEventType.BIRTH &&
            it.result == ReproductiveEventResult.SUCCESSFUL
    }

/**
 * Dato contextual rápido que se muestra en la fila de la lista de Hato — varía según
 * el estado actual de la cabra (spec: "dato contextual rápido según estado").
 */
fun goatContextualInfo(
    goat: Goat,
    today: LocalDate,
    milkLitersToday: Double?,
    nextExpectedBirth: LocalDate?,
    lastWeightDate: LocalDate?
): String = when (goat.currentStatus) {
    GoatStatus.IN_PRODUCTION -> milkLitersToday?.let { "${roundTo1Decimal(it)} L hoy" } ?: "Sin ordeño hoy"
    GoatStatus.PREGNANT -> nextExpectedBirth?.let { "Parto en ${today.daysUntil(it)} días" } ?: "Gestante"
    GoatStatus.DRY -> lastWeightDate?.let { "Última pesada: hace ${it.daysUntil(today)} días" } ?: "Sin pesadas registradas"
    GoatStatus.YOUNG_DOE, GoatStatus.KID, GoatStatus.BREEDING_BUCK -> "Edad: ${ageLabel(goat.birthDate, today)}"
    GoatStatus.RETIRED, GoatStatus.DECEASED -> goat.exitReason ?: "Sin motivo registrado"
}

/**
 * Composición racial de una cría nacida en la finca: el promedio de la de cada padre
 * (herencia 50/50 estándar). Si un padre no está en el sistema (semental externo) o no
 * tiene composición registrada, su aporte se trata como desconocido — no se inventa un
 * 50% "sin raza"; el resultado solo refleja la mitad conocida (la del otro padre) y el
 * admin puede completarlo manualmente en la ficha del cabrito.
 */
fun averageBreedComposition(
    motherComposition: List<BreedPercentage>,
    fatherComposition: List<BreedPercentage>
): List<BreedPercentage> {
    if (motherComposition.isEmpty() && fatherComposition.isEmpty()) return emptyList()
    val byBreed = linkedMapOf<String, Double>()
    motherComposition.forEach { byBreed[it.breedName] = (byBreed[it.breedName] ?: 0.0) + it.percentage / 2 }
    fatherComposition.forEach { byBreed[it.breedName] = (byBreed[it.breedName] ?: 0.0) + it.percentage / 2 }
    return byBreed.map { (name, percentage) -> BreedPercentage(name, percentage) }
}

/** Suma de porcentajes — referencia informativa en el formulario (idealmente 100). */
fun breedCompositionTotal(composition: List<BreedPercentage>): Double = composition.sumOf { it.percentage }

/** Etiqueta legible para la ficha técnica (ej. "100% Alpina", "50% Alpina · 50% Nubia"). */
fun breedCompositionLabel(composition: List<BreedPercentage>): String =
    if (composition.isEmpty()) {
        "Sin registrar"
    } else {
        composition.joinToString(" · ") { "${roundTo1Decimal(it.percentage)}% ${it.breedName}" }
    }
