package com.didiprogrammer.almacaprina.ui.admin.mas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.didiprogrammer.almacaprina.ui.components.AlmacaprinaCard

/**
 * Sección "Más" del bottom navigation: despliega Compras, Calendario de tareas y
 * Ajustes. Ajustes queda como "próximamente" — no es parte de este encargo (secciones 5 y 6).
 */
@Composable
fun AdminMasMenuScreen(
    onComprasClick: () -> Unit,
    onCalendarioClick: () -> Unit,
    onAjustesClick: () -> Unit
) {
    Scaffold(topBar = { TopAppBar(title = { Text("Más") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                MasMenuRow(icon = Icons.Outlined.ShoppingCart, label = "Compras", onClick = onComprasClick)
            }
            item {
                MasMenuRow(icon = Icons.AutoMirrored.Outlined.EventNote, label = "Calendario de tareas", onClick = onCalendarioClick)
            }
            item {
                MasMenuRow(icon = Icons.Outlined.Settings, label = "Ajustes (próximamente)", onClick = onAjustesClick)
            }
        }
    }
}

@Composable
private fun MasMenuRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    AlmacaprinaCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}
