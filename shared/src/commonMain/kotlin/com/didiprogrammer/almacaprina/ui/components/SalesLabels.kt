package com.didiprogrammer.almacaprina.ui.components

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.customer_type_business
import almacaprina.shared.generated.resources.customer_type_individual
import almacaprina.shared.generated.resources.payment_method_cash
import almacaprina.shared.generated.resources.payment_method_other
import almacaprina.shared.generated.resources.payment_method_transfer
import almacaprina.shared.generated.resources.payment_status_paid
import almacaprina.shared.generated.resources.payment_status_pending
import androidx.compose.runtime.Composable
import com.didiprogrammer.almacaprina.domain.model.CustomerType
import com.didiprogrammer.almacaprina.domain.model.PaymentMethod
import com.didiprogrammer.almacaprina.domain.model.PaymentStatus
import org.jetbrains.compose.resources.stringResource

/** Etiquetas en español de los enums del módulo Ventas — solo se pueden llamar desde contexto @Composable. */

@Composable
fun CustomerType.label(): String = stringResource(
    when (this) {
        CustomerType.INDIVIDUAL -> Res.string.customer_type_individual
        CustomerType.BUSINESS -> Res.string.customer_type_business
    }
)

@Composable
fun PaymentMethod.label(): String = stringResource(
    when (this) {
        PaymentMethod.CASH -> Res.string.payment_method_cash
        PaymentMethod.TRANSFER -> Res.string.payment_method_transfer
        PaymentMethod.OTHER -> Res.string.payment_method_other
    }
)

@Composable
fun PaymentStatus.label(): String = stringResource(
    when (this) {
        PaymentStatus.PAID -> Res.string.payment_status_paid
        PaymentStatus.PENDING -> Res.string.payment_status_pending
    }
)
