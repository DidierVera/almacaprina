package com.didiprogrammer.almacaprina.ui.admin.catalogo

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_catalog_active_label
import almacaprina.shared.generated.resources.admin_catalog_delete_confirm_message
import almacaprina.shared.generated.resources.admin_catalog_delete_confirm_title
import almacaprina.shared.generated.resources.admin_catalog_delete_content_description
import almacaprina.shared.generated.resources.admin_goat_form_name_label
import almacaprina.shared.generated.resources.admin_insumo_form_edit_title
import almacaprina.shared.generated.resources.admin_insumo_form_new_title
import almacaprina.shared.generated.resources.admin_insumo_form_notes_label
import almacaprina.shared.generated.resources.admin_insumo_form_package_name_label
import almacaprina.shared.generated.resources.admin_insumo_form_package_size_label
import almacaprina.shared.generated.resources.admin_insumo_form_purchase_package_hint
import almacaprina.shared.generated.resources.admin_insumo_form_purchase_package_label
import almacaprina.shared.generated.resources.admin_insumo_form_unit_of_measure_label
import almacaprina.shared.generated.resources.admin_product_form_category_label
import almacaprina.shared.generated.resources.admin_settings_delete_button
import almacaprina.shared.generated.resources.common_cancel
import almacaprina.shared.generated.resources.common_save_button
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.didiprogrammer.almacaprina.domain.model.Insumo
import com.didiprogrammer.almacaprina.domain.model.InsumoCategory
import com.didiprogrammer.almacaprina.domain.model.UnitOfMeasure
import com.didiprogrammer.almacaprina.ui.components.label
import org.jetbrains.compose.resources.stringResource

/**
 * Catálogo > Insumos > "+ Nuevo insumo" / editar uno existente.
 * `existing` nulo = alta; no nulo = edición. `onDelete` nulo = no se ofrece eliminar.
 */
@Composable
fun InsumoFormDialog(
    existing: Insumo?,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        category: InsumoCategory,
        unitOfMeasure: UnitOfMeasure,
        active: Boolean,
        purchasePackageLabel: String?,
        purchasePackageSize: Double?,
        notes: String?
    ) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: InsumoCategory.FEED) }
    var unitOfMeasure by remember { mutableStateOf(existing?.unitOfMeasure ?: UnitOfMeasure.KG) }
    var active by remember { mutableStateOf(existing?.active ?: true) }
    var purchasePackageLabel by remember { mutableStateOf(existing?.purchasePackageLabel ?: "") }
    var purchasePackageSizeText by remember { mutableStateOf(existing?.purchasePackageSize?.toString() ?: "") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm && existing != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(Res.string.admin_catalog_delete_confirm_title, existing.name)) },
            text = { Text(stringResource(Res.string.admin_catalog_delete_confirm_message)) },
            confirmButton = {
                Button(onClick = { showDeleteConfirm = false; onDelete?.invoke() }) { Text(stringResource(Res.string.admin_settings_delete_button)) }
            },
            dismissButton = { OutlinedButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(Res.string.common_cancel)) } }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(if (existing == null) Res.string.admin_insumo_form_new_title else Res.string.admin_insumo_form_edit_title))
                if (existing != null && onDelete != null) {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = stringResource(Res.string.admin_catalog_delete_content_description))
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(Res.string.admin_goat_form_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text(stringResource(Res.string.admin_product_form_category_label))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(InsumoCategory.entries) { c ->
                        FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c.label()) })
                    }
                }
                Text(stringResource(Res.string.admin_insumo_form_unit_of_measure_label))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(UnitOfMeasure.entries) { u ->
                        FilterChip(selected = unitOfMeasure == u, onClick = { unitOfMeasure = u }, label = { Text(u.label()) })
                    }
                }
                Text(stringResource(Res.string.admin_insumo_form_purchase_package_label))
                Text(
                    stringResource(Res.string.admin_insumo_form_purchase_package_hint, unitOfMeasure.label()),
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = purchasePackageLabel,
                    onValueChange = { purchasePackageLabel = it },
                    label = { Text(stringResource(Res.string.admin_insumo_form_package_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = purchasePackageSizeText,
                    onValueChange = { purchasePackageSizeText = it },
                    label = { Text(stringResource(Res.string.admin_insumo_form_package_size_label, unitOfMeasure.label())) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(Res.string.admin_insumo_form_notes_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(Res.string.admin_catalog_active_label))
                    Switch(checked = active, onCheckedChange = { active = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(
                        name.trim(),
                        category,
                        unitOfMeasure,
                        active,
                        purchasePackageLabel.trim().ifBlank { null },
                        purchasePackageSizeText.toDoubleOrNull(),
                        notes.trim().ifBlank { null }
                    )
                }
            ) { Text(stringResource(Res.string.common_save_button)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_cancel)) } }
    )
}
