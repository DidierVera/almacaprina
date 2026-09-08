package com.didiprogrammer.almacaprina.ui.admin.catalogo

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_goat_form_name_label
import almacaprina.shared.generated.resources.admin_packaging_form_deposit_label
import almacaprina.shared.generated.resources.admin_packaging_form_edit_title
import almacaprina.shared.generated.resources.admin_packaging_form_new_title
import almacaprina.shared.generated.resources.admin_packaging_form_returnable_label
import almacaprina.shared.generated.resources.admin_packaging_form_unit_cost_label
import almacaprina.shared.generated.resources.common_cancel
import almacaprina.shared.generated.resources.common_save_button
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
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
import com.didiprogrammer.almacaprina.domain.model.Packaging
import org.jetbrains.compose.resources.stringResource

/**
 * Catálogo > Envases > "+ Nuevo envase" / editar uno existente.
 * `existing` nulo = alta; no nulo = edición.
 *
 * NOTA: `Packaging` no tiene un campo `active` en docs/data_model.md (a diferencia de
 * Product e Insumo), así que aquí solo se puede editar, no "desactivar". Si quieres poder
 * descontinuar un envase sin borrar su historial de compras/ventas, avísame y agrego el
 * campo (requiere una migración SQL nueva).
 */
@Composable
fun PackagingFormDialog(
    existing: Packaging?,
    onDismiss: () -> Unit,
    onSave: (name: String, isReturnable: Boolean, depositAmount: Double?, unitCost: Double) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var isReturnable by remember { mutableStateOf(existing?.isReturnable ?: false) }
    var depositText by remember { mutableStateOf(existing?.depositAmount?.toString() ?: "") }
    var unitCostText by remember { mutableStateOf(existing?.unitCost?.toString() ?: "") }
    val unitCost = unitCostText.toDoubleOrNull()
    val deposit = depositText.toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (existing == null) Res.string.admin_packaging_form_new_title else Res.string.admin_packaging_form_edit_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(Res.string.admin_goat_form_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(Res.string.admin_packaging_form_returnable_label))
                    Switch(checked = isReturnable, onCheckedChange = { isReturnable = it })
                }
                if (isReturnable) {
                    OutlinedTextField(
                        value = depositText,
                        onValueChange = { depositText = it },
                        label = { Text(stringResource(Res.string.admin_packaging_form_deposit_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = unitCostText,
                    onValueChange = { unitCostText = it },
                    label = { Text(stringResource(Res.string.admin_packaging_form_unit_cost_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && unitCost != null && unitCost >= 0 && (!isReturnable || (deposit != null && deposit >= 0)),
                onClick = {
                    unitCost?.let { onSave(name.trim(), isReturnable, if (isReturnable) deposit else null, it) }
                }
            ) { Text(stringResource(Res.string.common_save_button)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_cancel)) } }
    )
}
