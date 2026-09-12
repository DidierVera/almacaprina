package com.didiprogrammer.almacaprina.ui.admin.catalogo

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_breed_form_edit_title
import almacaprina.shared.generated.resources.admin_breed_form_new_title
import almacaprina.shared.generated.resources.admin_breed_form_prefix_label
import almacaprina.shared.generated.resources.admin_catalog_delete_confirm_message
import almacaprina.shared.generated.resources.admin_catalog_delete_confirm_title
import almacaprina.shared.generated.resources.admin_catalog_delete_content_description
import almacaprina.shared.generated.resources.admin_goat_form_name_label
import almacaprina.shared.generated.resources.admin_settings_delete_button
import almacaprina.shared.generated.resources.common_cancel
import almacaprina.shared.generated.resources.common_save_button
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.didiprogrammer.almacaprina.domain.model.Breed
import org.jetbrains.compose.resources.stringResource

/** Catálogo > Razas > "+ Nueva raza" / editar una existente. `existing` nulo = alta;
 * `onDelete` nulo = no se ofrece eliminar. */
@Composable
fun BreedFormDialog(
    existing: Breed?,
    onDismiss: () -> Unit,
    onSave: (name: String, prefix: String?) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var prefix by remember { mutableStateOf(existing?.prefix ?: "") }
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
                Text(stringResource(if (existing == null) Res.string.admin_breed_form_new_title else Res.string.admin_breed_form_edit_title))
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
                OutlinedTextField(
                    value = prefix,
                    onValueChange = { prefix = it },
                    label = { Text(stringResource(Res.string.admin_breed_form_prefix_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onSave(name.trim(), prefix.trim().ifBlank { null }) }
            ) { Text(stringResource(Res.string.common_save_button)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_cancel)) } }
    )
}
