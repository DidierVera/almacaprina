package com.didiprogrammer.almacaprina.ui.components

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.common_close
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
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
import org.jetbrains.compose.resources.stringResource

/**
 * Fila de filtros de una sola selección. Con pocas opciones se muestra como chips en un
 * LazyRow; al superar [chipThreshold] opciones se colapsa en un selector modal — mismo patrón
 * de diálogo que GoatPickerDialog/BreedPickerDialog — para no forzar scroll horizontal ni
 * saturar la pantalla.
 */
@Composable
fun <T> ChipFilterRow(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    pickerTitle: String,
    modifier: Modifier = Modifier,
    chipThreshold: Int = 3
) {
    if (options.size <= chipThreshold) {
        LazyRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(options) { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    label = { Text(label(option)) }
                )
            }
        }
    } else {
        var showPicker by remember { mutableStateOf(false) }

        Box(modifier = modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = label(selected),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        showPicker = true
                    }
            )
        }

        if (showPicker) {
            AlertDialog(
                onDismissRequest = { showPicker = false },
                title = { Text(pickerTitle) },
                text = {
                    LazyColumn(
                        modifier = Modifier.height(280.dp).padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(options) { option ->
                            TextButton(onClick = {
                                onSelect(option)
                                showPicker = false
                            }) {
                                Text(label(option))
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = { TextButton(onClick = { showPicker = false }) { Text(stringResource(Res.string.common_close)) } }
            )
        }
    }
}
