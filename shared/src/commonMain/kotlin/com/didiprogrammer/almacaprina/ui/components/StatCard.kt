package com.didiprogrammer.almacaprina.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.didiprogrammer.almacaprina.ui.theme.Borde
import com.didiprogrammer.almacaprina.ui.theme.ShapeExtraLarge
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import com.didiprogrammer.almacaprina.ui.theme.Superficie
import com.didiprogrammer.almacaprina.ui.theme.Tinta
import com.didiprogrammer.almacaprina.ui.theme.TintaSuave
import androidx.compose.ui.unit.dp

/**
 * Card grande de dashboard (ej. "Producción vs. meta"): encabezado en mayúsculas
 * (labelSmall), cifra grande, contenido libre debajo (barra de progreso, texto secundario).
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueStyle: TextStyle = MaterialTheme.typography.displayMedium,
    content: @Composable (androidx.compose.foundation.layout.ColumnScope.() -> Unit)? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = ShapeExtraLarge,
        color = Superficie,
        border = BorderStroke(1.dp, Borde)
    ) {
        Column(modifier = Modifier.padding(Spacing.xl)) {
            Text(title.uppercase(), style = MaterialTheme.typography.labelSmall, color = TintaSuave)
            Spacer(Modifier.height(Spacing.xs))
            Text(value, style = valueStyle, color = Tinta)
            if (content != null) {
                Spacer(Modifier.height(Spacing.md))
                content()
            }
        }
    }
}
