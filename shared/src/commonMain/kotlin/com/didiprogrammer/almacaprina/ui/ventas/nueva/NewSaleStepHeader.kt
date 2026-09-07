package com.didiprogrammer.almacaprina.ui.ventas.nueva

import androidx.compose.runtime.Composable
import com.didiprogrammer.almacaprina.ui.components.ScreenHeaderWithBack

/** Encabezado único de los 4 pasos de "Nueva venta" — ver mockups NUEVA VENTA · N DE 4. */
@Composable
fun NewSaleStepHeader(step: Int, title: String, onBack: () -> Unit, subtitle: String? = null) {
    ScreenHeaderWithBack(eyebrow = "Nueva venta · $step de 4", title = title, onBack = onBack, subtitle = subtitle)
}
