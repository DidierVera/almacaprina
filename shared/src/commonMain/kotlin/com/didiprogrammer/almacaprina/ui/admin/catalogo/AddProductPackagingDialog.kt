package com.didiprogrammer.almacaprina.ui.admin.catalogo

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_product_packaging_dialog_default_label
import almacaprina.shared.generated.resources.admin_product_packaging_dialog_packaging_label
import almacaprina.shared.generated.resources.admin_product_packaging_dialog_title
import almacaprina.shared.generated.resources.common_cancel
import almacaprina.shared.generated.resources.common_save_button
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.didiprogrammer.almacaprina.domain.model.Packaging
import org.jetbrains.compose.resources.stringResource

/** Catálogo > Empaques > "+ Asociar envase al producto". */
@Composable
fun AddProductPackagingDialog(
    availablePackagings: List<Packaging>,
    onDismiss: () -> Unit,
    onSave: (packagingId: String, isDefault: Boolean) -> Unit
) {
    var selectedPackagingId by remember { mutableStateOf<String?>(null) }
    var isDefault by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.admin_product_packaging_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(Res.string.admin_product_packaging_dialog_packaging_label))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(availablePackagings, key = { it.id }) { packaging ->
                        FilterChip(
                            selected = selectedPackagingId == packaging.id,
                            onClick = { selectedPackagingId = packaging.id },
                            label = { Text(packaging.name) }
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(Res.string.admin_product_packaging_dialog_default_label))
                    Switch(checked = isDefault, onCheckedChange = { isDefault = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedPackagingId != null,
                onClick = { selectedPackagingId?.let { onSave(it, isDefault) } }
            ) { Text(stringResource(Res.string.common_save_button)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_cancel)) } }
    )
}
