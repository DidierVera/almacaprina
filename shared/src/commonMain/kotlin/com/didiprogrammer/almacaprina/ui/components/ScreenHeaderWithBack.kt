package com.didiprogrammer.almacaprina.ui.components

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.common_back_content_description
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.didiprogrammer.almacaprina.ui.theme.Borde
import com.didiprogrammer.almacaprina.ui.theme.ShapeLarge
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import com.didiprogrammer.almacaprina.ui.theme.Superficie
import com.didiprogrammer.almacaprina.ui.theme.Tinta
import com.didiprogrammer.almacaprina.ui.theme.TintaSuave
import org.jetbrains.compose.resources.stringResource

/**
 * Encabezado único con botón "volver" circular — mismo patrón visual en todos los flujos
 * con back stack de Ventas y Campo (ej. "NUEVA VENTA · 1 DE 4", "COBRAR PENDIENTES").
 */
@Composable
fun ScreenHeaderWithBack(
    eyebrow: String?,
    title: String,
    onBack: () -> Unit,
    subtitle: String? = null
) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Surface(
            shape = ShapeLarge,
            color = Superficie,
            border = BorderStroke(1.dp, Borde),
            modifier = Modifier.size(44.dp).clickable(onClick = onBack)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = stringResource(Res.string.common_back_content_description), tint = Tinta)
            }
        }
        Column {
            if (eyebrow != null) {
                Text(eyebrow.uppercase(), style = MaterialTheme.typography.labelSmall, color = TintaSuave)
            }
            Text(title, style = MaterialTheme.typography.headlineMedium, color = Tinta)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
            }
        }
    }
}
