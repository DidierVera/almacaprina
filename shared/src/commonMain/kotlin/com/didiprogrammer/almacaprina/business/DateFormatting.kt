package com.didiprogrammer.almacaprina.business

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

private val spanishWeekdays = mapOf(
    DayOfWeek.MONDAY to "Lunes",
    DayOfWeek.TUESDAY to "Martes",
    DayOfWeek.WEDNESDAY to "Miércoles",
    DayOfWeek.THURSDAY to "Jueves",
    DayOfWeek.FRIDAY to "Viernes",
    DayOfWeek.SATURDAY to "Sábado",
    DayOfWeek.SUNDAY to "Domingo"
)

private val spanishMonths = mapOf(
    Month.JANUARY to "enero",
    Month.FEBRUARY to "febrero",
    Month.MARCH to "marzo",
    Month.APRIL to "abril",
    Month.MAY to "mayo",
    Month.JUNE to "junio",
    Month.JULY to "julio",
    Month.AUGUST to "agosto",
    Month.SEPTEMBER to "septiembre",
    Month.OCTOBER to "octubre",
    Month.NOVEMBER to "noviembre",
    Month.DECEMBER to "diciembre"
)

/** "Sábado 29 de agosto" — encabezado de fecha único de Ventas y Campo (ver mockups). */
fun formatLongSpanishDate(date: LocalDate): String {
    val weekday = spanishWeekdays[date.dayOfWeek] ?: date.dayOfWeek.name
    val month = spanishMonths[date.month] ?: date.month.name.lowercase()
    return "$weekday ${date.day} de $month"
}
