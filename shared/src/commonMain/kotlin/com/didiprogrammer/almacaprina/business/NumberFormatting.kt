package com.didiprogrammer.almacaprina.business

import kotlin.math.round

/** Redondeo a 1 decimal reutilizado por varias pantallas al mostrar litros. */
fun roundTo1Decimal(value: Double): Double = round(value * 10) / 10

/** Formato de moneda reutilizado en toda la app (ej. "$12.000 COP"). */
fun formatCurrency(value: Double, currency: String): String {
    val rounded = round(value).toLong()
    val withThousands = rounded.toString().reversed().chunked(3).joinToString(".").reversed()
    return "$$withThousands $currency"
}

/** Formato compacto de cantidades: sin decimales si el valor es entero (ej. "50" o "2.5"). */
fun formatQuantity(value: Double): String {
    val rounded = roundTo1Decimal(value)
    return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString() else rounded.toString()
}
