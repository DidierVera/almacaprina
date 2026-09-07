package com.didiprogrammer.almacaprina.business

import com.didiprogrammer.almacaprina.domain.model.Packaging
import com.didiprogrammer.almacaprina.domain.model.PaymentStatus
import com.didiprogrammer.almacaprina.domain.model.Sale
import kotlinx.datetime.LocalDate

/** Ingresos reales del negocio (sin depósitos) para ventas dentro de [start, end], ambos inclusive. */
fun revenueForPeriod(sales: List<Sale>, start: LocalDate, end: LocalDate): Double =
    sales.filter { it.date >= start && it.date <= end }.sumOf { it.revenue() }

/**
 * PendingSalesBalance — Σ Sale.total_value donde payment_status = pending (cartera por cobrar).
 * No se acota por período: es el saldo pendiente actual, sin importar cuándo se hizo la venta.
 * `packagingsById` permite resolver el depósito de cada venta con envase retornable.
 */
fun pendingSalesBalance(sales: List<Sale>, packagingsById: Map<String, Packaging>): Double =
    sales
        .filter { it.paymentStatus == PaymentStatus.PENDING }
        .sumOf { sale -> sale.totalValue(sale.packagingId?.let { packagingsById[it] }) }

/** NetMargin — Σ Sale.total_value − Σ Purchase.total_cost, para un período dado. */
fun netMargin(
    sales: List<Sale>,
    packagingsById: Map<String, Packaging>,
    totalPurchaseCostInPeriod: Double,
    start: LocalDate,
    end: LocalDate
): Double {
    val revenue = sales
        .filter { it.date >= start && it.date <= end }
        .sumOf { sale -> sale.totalValue(sale.packagingId?.let { packagingsById[it] }) }
    return revenue - totalPurchaseCostInPeriod
}
