package com.didiprogrammer.almacaprina.ui.admin.catalogo

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
        title = { Text("Agregar insumo a la receta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Insumo")
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
                    label = { Text("Cantidad esperada por unidad de producto") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedInsumoId != null && quantity != null && quantity > 0,
                onClick = { selectedInsumoId?.let { id -> quantity?.let { onSave(id, it) } } }
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
