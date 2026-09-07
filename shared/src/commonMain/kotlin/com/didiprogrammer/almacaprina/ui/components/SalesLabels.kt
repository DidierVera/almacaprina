package com.didiprogrammer.almacaprina.ui.components

import com.didiprogrammer.almacaprina.domain.model.CustomerType
import com.didiprogrammer.almacaprina.domain.model.PaymentMethod
import com.didiprogrammer.almacaprina.domain.model.PaymentStatus

/** Etiquetas en español de los enums del módulo Ventas — funciones puras, sin contexto @Composable. */

fun CustomerType.label(): String = when (this) {
    CustomerType.INDIVIDUAL -> "Individual"
    CustomerType.BUSINESS -> "Negocio"
}

fun PaymentMethod.label(): String = when (this) {
    PaymentMethod.CASH -> "Efectivo"
    PaymentMethod.TRANSFER -> "Transferencia"
    PaymentMethod.OTHER -> "Otro"
}

fun PaymentStatus.label(): String = when (this) {
    PaymentStatus.PAID -> "Pagado"
    PaymentStatus.PENDING -> "Fiado"
}
