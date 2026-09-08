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
import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_care_task_list_title
import almacaprina.shared.generated.resources.admin_mas_ajustes_label
import almacaprina.shared.generated.resources.admin_mas_compras_label
import almacaprina.shared.generated.resources.admin_tab_mas
import com.didiprogrammer.almacaprina.ui.components.AlmacaprinaCard
import org.jetbrains.compose.resources.stringResource

/**
 * Sección "Más" del bottom navigation: despliega Compras, Calendario de tareas y
 * Ajustes.
 */
@Composable
fun AdminMasMenuScreen(
    onComprasClick: () -> Unit,
    onCalendarioClick: () -> Unit,
    onAjustesClick: () -> Unit
) {
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(Res.string.admin_tab_mas)) }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                MasMenuRow(icon = Icons.Outlined.ShoppingCart, label = stringResource(Res.string.admin_mas_compras_label), onClick = onComprasClick)
            }
            item {
                MasMenuRow(icon = Icons.AutoMirrored.Outlined.EventNote, label = stringResource(Res.string.admin_care_task_list_title), onClick = onCalendarioClick)
            }
            item {
                MasMenuRow(icon = Icons.Outlined.Settings, label = stringResource(Res.string.admin_mas_ajustes_label), onClick = onAjustesClick)
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
