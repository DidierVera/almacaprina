package com.didiprogrammer.almacaprina.ui.admin.catalogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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

/**
 * Catálogo > Insumos > "+ Nuevo insumo" / editar uno existente.
 * `existing` nulo = alta; no nulo = edición.
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
    ) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: InsumoCategory.FEED) }
    var unitOfMeasure by remember { mutableStateOf(existing?.unitOfMeasure ?: UnitOfMeasure.KG) }
    var active by remember { mutableStateOf(existing?.active ?: true) }
    var purchasePackageLabel by remember { mutableStateOf(existing?.purchasePackageLabel ?: "") }
    var purchasePackageSizeText by remember { mutableStateOf(existing?.purchasePackageSize?.toString() ?: "") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Nuevo insumo" else "Editar insumo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text("Categoría")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(InsumoCategory.entries) { c ->
                        FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c.label()) })
                    }
                }
                Text("Unidad de medida")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(UnitOfMeasure.entries) { u ->
                        FilterChip(selected = unitOfMeasure == u, onClick = { unitOfMeasure = u }, label = { Text(u.label()) })
                    }
                }
                Text("Empaque de compra (opcional)")
                Text(
                    "Si este insumo se compra por empaques (ej. una botella de 50 ml de cuajo), " +
                        "define aquí su nombre y contenido para que \"Nueva compra\" calcule solo la cantidad " +
                        "y el costo en ${unitOfMeasure.label()}.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = purchasePackageLabel,
                    onValueChange = { purchasePackageLabel = it },
                    label = { Text("Nombre del empaque (ej. Botella, Bulto, Saco)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = purchasePackageSizeText,
                    onValueChange = { purchasePackageSizeText = it },
                    label = { Text("Contenido por empaque (en ${unitOfMeasure.label()})") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observaciones (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Activo")
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
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
