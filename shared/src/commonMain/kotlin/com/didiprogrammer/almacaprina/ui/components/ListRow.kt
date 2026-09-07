package com.didiprogrammer.almacaprina.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.didiprogrammer.almacaprina.ui.theme.Borde
import com.didiprogrammer.almacaprina.ui.theme.ShapeMedium
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import com.didiprogrammer.almacaprina.ui.theme.Superficie
import com.didiprogrammer.almacaprina.ui.theme.Tinta
import com.didiprogrammer.almacaprina.ui.theme.TintaOff
import com.didiprogrammer.almacaprina.ui.theme.TintaSuave

/**
 * Fila de lista única del módulo — radio 16, punto indicador de 8.dp a la izquierda
 * cuando hay estado, chevron a la derecha cuando es navegable.
 */
@Composable
fun ListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    indicatorColor: Color? = null,
    trailingText: String? = null,
    navigable: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
        shape = ShapeMedium,
        color = Superficie,
        border = BorderStroke(1.dp, Borde)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.lg).heightIn(min = 44.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            if (indicatorColor != null) {
                Box(modifier = Modifier.size(8.dp).background(indicatorColor, CircleShape))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = Tinta)
                subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = TintaSuave) }
            }
            trailingText?.let { Text(it, style = MaterialTheme.typography.titleMedium, color = Tinta) }
            trailingContent?.invoke()
            if (navigable) {
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TintaOff)
            }
        }
    }
}
