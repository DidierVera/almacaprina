package com.didiprogrammer.almacaprina.ui.ventas.nueva

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.new_sale_step_eyebrow
import androidx.compose.runtime.Composable
import com.didiprogrammer.almacaprina.ui.components.ScreenHeaderWithBack
import org.jetbrains.compose.resources.stringResource

/** Encabezado único de los 4 pasos de "Nueva venta" — ver mockups NUEVA VENTA · N DE 4. */
@Composable
fun NewSaleStepHeader(step: Int, title: String, onBack: () -> Unit, subtitle: String? = null) {
    ScreenHeaderWithBack(
        eyebrow = stringResource(Res.string.new_sale_step_eyebrow, step),
        title = title,
        onBack = onBack,
        subtitle = subtitle
    )
}
