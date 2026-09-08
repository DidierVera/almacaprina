package com.didiprogrammer.almacaprina.ui.admin.catalogo

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_recipe_item_dialog_title
import almacaprina.shared.generated.resources.admin_recipe_item_insumo_label
import almacaprina.shared.generated.resources.admin_recipe_item_quantity_label
import almacaprina.shared.generated.resources.common_cancel
import almacaprina.shared.generated.resources.common_save_button
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.didiprogrammer.almacaprina.domain.model.Insumo
import org.jetbrains.compose.resources.stringResource

/** Catálogo > Recetas > "+ Agregar insumo a la receta". */
@Composable
fun AddRecipeItemDialog(
    insumos: List<Insumo>,
    onDismiss: () -> Unit,
    onSave: (insumoId: String, quantityPerOutputUnit: Double) -> Unit
) {
    var selectedInsumoId by remember { mutableStateOf<String?>(null) }
    var quantityText by remember { mutableStateOf("") }
    val quantity = quantityText.toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.admin_recipe_item_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(Res.string.admin_recipe_item_insumo_label))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(insumos) { insumo ->
                        FilterChip(
                            selected = selectedInsumoId == insumo.id,
                            onClick = { selectedInsumoId = insumo.id },
                            label = { Text(insumo.name) }
                        )
                    }
                }
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text(stringResource(Res.string.admin_recipe_item_quantity_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedInsumoId != null && quantity != null && quantity > 0,
                onClick = { selectedInsumoId?.let { id -> quantity?.let { onSave(id, it) } } }
            ) { Text(stringResource(Res.string.common_save_button)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_cancel)) } }
    )
}
