package com.didiprogrammer.almacaprina.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.didiprogrammer.almacaprina.ui.theme.AmbarBorde
import com.didiprogrammer.almacaprina.ui.theme.AmbarFondo
import com.didiprogrammer.almacaprina.ui.theme.AmbarTexto
import com.didiprogrammer.almacaprina.ui.theme.ShapeMedium
import com.didiprogrammer.almacaprina.ui.theme.Spacing

/** Fila de alerta única del módulo — fondo AmbarFondo, borde AmbarBorde, texto AmbarTexto. */
@Composable
fun AlertRow(
    message: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
        shape = ShapeMedium,
        color = AmbarFondo,
        border = BorderStroke(1.dp, AmbarBorde)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.lg).heightIn(min = 44.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = AmbarTexto,
                modifier = Modifier.weight(1f)
            )
            if (onClick != null) {
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = AmbarTexto)
            }
        }
    }
}
