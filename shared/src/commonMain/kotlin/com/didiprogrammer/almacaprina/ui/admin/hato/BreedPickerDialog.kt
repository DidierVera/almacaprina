package com.didiprogrammer.almacaprina.ui.admin.hato

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_goat_form_breed_picker_create_button
import almacaprina.shared.generated.resources.admin_goat_form_breed_picker_title
import almacaprina.shared.generated.resources.admin_hato_search_placeholder
import almacaprina.shared.generated.resources.common_close
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import com.didiprogrammer.almacaprina.domain.model.Breed
import org.jetbrains.compose.resources.stringResource

/**
 * Selector con buscador para elegir una raza del maestro de razas — usado en cada fila de
 * composición racial del formulario de cabra. Si el nombre buscado no existe todavía, permite
 * crearla ahí mismo (para no obligar a salir a Catálogo > Razas a mitad del formulario).
 */
@Composable
fun BreedPickerDialog(
    breeds: List<Breed>,
    onDismiss: () -> Unit,
    onSelect: (Breed) -> Unit,
    onCreateNew: (name: String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, breeds) {
        breeds.filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
    }
    val trimmedQuery = query.trim()
    val exactMatchExists = breeds.any { it.name.equals(trimmedQuery, ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.admin_goat_form_breed_picker_title)) },
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
                    items(filtered, key = { it.id }) { breed ->
                        TextButton(onClick = { onSelect(breed) }) {
                            Text(breed.name)
                        }
                    }
                    if (trimmedQuery.isNotEmpty() && !exactMatchExists) {
                        item {
                            TextButton(onClick = { onCreateNew(trimmedQuery) }) {
                                Icon(Icons.Outlined.Add, contentDescription = null)
                                Text(" " + stringResource(Res.string.admin_goat_form_breed_picker_create_button, trimmedQuery))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_close)) } }
    )
}
