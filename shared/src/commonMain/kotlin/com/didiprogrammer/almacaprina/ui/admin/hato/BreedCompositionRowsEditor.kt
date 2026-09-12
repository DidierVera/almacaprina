package com.didiprogrammer.almacaprina.ui.admin.hato

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_goat_form_add_breed_button
import almacaprina.shared.generated.resources.admin_goat_form_breed_select_placeholder
import almacaprina.shared.generated.resources.admin_goat_form_breed_total_label
import almacaprina.shared.generated.resources.admin_goat_form_remove_breed_content_description
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.didiprogrammer.almacaprina.business.formatQuantity
import org.jetbrains.compose.resources.stringResource

/**
 * Editor de filas raza/porcentaje reutilizado tanto para la composición racial propia de una
 * cabra como para la de un semental externo (Nueva cabra, Nueva monta) — ver CLAUDE.md.
 */
@Composable
fun BreedCompositionRowsEditor(
    rows: List<BreedCompositionRow>,
    onAddRow: () -> Unit,
    onRemoveRow: (rowId: String) -> Unit,
    onPercentageChanged: (rowId: String, value: String) -> Unit,
    onPickBreed: (rowId: String) -> Unit
) {
    Column {
        rows.forEach { row ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = { onPickBreed(row.rowId) }, modifier = Modifier.weight(1f)) {
                    Text(row.breedName.ifBlank { stringResource(Res.string.admin_goat_form_breed_select_placeholder) })
                }
                OutlinedTextField(
                    value = row.percentageText,
                    onValueChange = { onPercentageChanged(row.rowId, it) },
                    label = { Text("%") },
                    modifier = Modifier.width(90.dp),
                    singleLine = true
                )
                IconButton(onClick = { onRemoveRow(row.rowId) }) {
                    Icon(Icons.Outlined.Close, contentDescription = stringResource(Res.string.admin_goat_form_remove_breed_content_description))
                }
            }
        }
        TextButton(onClick = onAddRow) {
            Icon(Icons.Outlined.Add, contentDescription = null)
            Text(" " + stringResource(Res.string.admin_goat_form_add_breed_button))
        }
        if (rows.isNotEmpty()) {
            Text(
                stringResource(Res.string.admin_goat_form_breed_total_label, formatQuantity(rows.toBreedComposition().sumOf { it.percentage })),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
