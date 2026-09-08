package com.didiprogrammer.almacaprina.ui.admin.hato

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_goat_form_goat_with_tag
import almacaprina.shared.generated.resources.admin_goat_picker_none_option
import almacaprina.shared.generated.resources.admin_hato_search_placeholder
import almacaprina.shared.generated.resources.common_close
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
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
import com.didiprogrammer.almacaprina.domain.model.Goat
import org.jetbrains.compose.resources.stringResource

/**
 * Selector con buscador interno para elegir madre/padre entre las cabras ya
 * registradas — reutilizable en cualquier formulario que necesite elegir una cabra.
 */
@Composable
fun GoatPickerDialog(
    title: String,
    candidates: List<Goat>,
    onDismiss: () -> Unit,
    onSelect: (Goat?) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, candidates) {
        candidates.filter {
            query.isBlank() ||
                it.name.contains(query, ignoreCase = true) ||
                it.tagNumber.contains(query, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(stringResource(Res.string.admin_hato_search_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                LazyColumn(
                    modifier = Modifier.height(280.dp).padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item {
                        TextButton(onClick = { onSelect(null) }) {
                            Text(stringResource(Res.string.admin_goat_picker_none_option))
                        }
                    }
                    items(filtered, key = { it.id }) { goat ->
                        TextButton(onClick = { onSelect(goat) }) {
                            Text(stringResource(Res.string.admin_goat_form_goat_with_tag, goat.name, goat.tagNumber))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_close)) } }
    )
}
