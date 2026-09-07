package com.didiprogrammer.almacaprina.business

import com.didiprogrammer.almacaprina.domain.model.DepositMovementType
import com.didiprogrammer.almacaprina.domain.model.Packaging
import com.didiprogrammer.almacaprina.domain.model.PackagingDepositTransaction
import com.didiprogrammer.almacaprina.domain.model.PaymentStatus
import com.didiprogrammer.almacaprina.domain.model.Sale
import kotlinx.datetime.LocalDate

/**
 * Funciones de negocio nuevas para el módulo Ventas. Código ADITIVO — `Sale.totalValue`,
 * `Sale.depositCharged`, `Sale.depositRefunded` y `pendingSalesBalance` (el total agregado
 * usado hoy en Inicio de Admin) ya existían en CostCalculations.kt/FinancialCalculations.kt
 * y se reutilizan tal cual, sin modificarlos.
 */

/** Saldo pendiente de un cliente — fila de "Cobrar pendientes". */
data class PendingCustomerBalance(
    val customerId: String,
    val totalPending: Double,
    val pendingSalesCount: Int,
    val oldestPendingDate: LocalDate
)

/**
 * PendingSalesBalance desglosado por cliente, ordenado por deuda más antigua primero
 * (ver mockup "Cobrar pendientes" — "Deuda más antigua primero").
 */
fun pendingBalancesByCustomer(sales: List<Sale>, packagingsById: Map<String, Packaging>): List<PendingCustomerBalance> =
    sales
        .filter { it.paymentStatus == PaymentStatus.PENDING }
        .groupBy { it.customerId }
        .map { (customerId, customerSales) ->
            PendingCustomerBalance(
                customerId = customerId,
                totalPending = customerSales.sumOf { sale -> sale.totalValue(sale.packagingId?.let { packagingsById[it] }) },
                pendingSalesCount = customerSales.size,
                oldestPendingDate = customerSales.minOf { it.date }
            )
        }
        .sortedBy { it.oldestPendingDate }

/** CustomerPackagingDepositBalance — Σ deposit_charged − Σ deposit_returned, por cliente. */
fun customerPackagingDepositBalance(
    transactions: List<PackagingDepositTransaction>,
    packagingsById: Map<String, Packaging>
): Map<String, Double> =
    transactions
        .groupBy { it.customerId }
        .mapValues { (_, txs) ->
            txs.sumOf { tx ->
                val amount = tx.quantity * (packagingsById[tx.packagingId]?.depositAmount ?: 0.0)
                if (tx.movementType == DepositMovementType.DEPOSIT_CHARGED) amount else -amount
            }
        }

/** TotalPackagingDepositsOut — Σ de CustomerPackagingDepositBalance de todos los clientes (en dinero). */
fun totalPackagingDepositsOut(transactions: List<PackagingDepositTransaction>, packagingsById: Map<String, Packaging>): Double =
    customerPackagingDepositBalance(transactions, packagingsById).values.sum()

/**
 * Conteo de envases retornables físicamente fuera (sin devolver), no el valor en dinero de
 * su depósito — es lo que muestra el Home de Ventas como "N envases fuera" (ver mockup).
 */
fun totalPackagingUnitsOut(transactions: List<PackagingDepositTransaction>): Int =
    transactions.sumOf { tx -> if (tx.movementType == DepositMovementType.DEPOSIT_CHARGED) tx.quantity else -tx.quantity }
