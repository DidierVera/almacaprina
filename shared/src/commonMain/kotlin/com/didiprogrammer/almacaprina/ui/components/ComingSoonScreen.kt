package com.didiprogrammer.almacaprina.ui.components

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.common_coming_soon_suffix
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource

/**
 * Placeholder temporal para secciones todavía no implementadas de la navegación
 * (se reemplaza pantalla por pantalla a medida que se construye cada sección).
 */
@Composable
fun ComingSoonScreen(title: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(Res.string.common_coming_soon_suffix, title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}
