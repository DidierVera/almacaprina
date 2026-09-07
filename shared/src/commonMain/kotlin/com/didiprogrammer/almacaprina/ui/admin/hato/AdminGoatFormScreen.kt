package com.didiprogrammer.almacaprina.ui.admin.hato

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.didiprogrammer.almacaprina.business.formatQuantity
import com.didiprogrammer.almacaprina.domain.model.GoatOrigin
import com.didiprogrammer.almacaprina.domain.model.GoatSex
import com.didiprogrammer.almacaprina.domain.model.GoatStatus
import com.didiprogrammer.almacaprina.ui.components.DateField
import com.didiprogrammer.almacaprina.ui.components.label
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.image.picker.toImageBitmap
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Sección 2, pantalla 2.3 — Alta y edición de una cabra (mismo formulario, `goatId` nulo = alta). */
@Composable
fun AdminGoatFormScreen(
    goatId: String?,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: AdminGoatFormViewModel = koinViewModel(parameters = { parametersOf(goatId) })
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showMotherPicker by remember { mutableStateOf(false) }
    var showFatherPicker by remember { mutableStateOf(false) }
    var fatherIsExternal by remember(uiState.isLoading) {
        mutableStateOf(uiState.externalFatherDescription.isNotBlank())
    }

    val pickerScope = rememberCoroutineScope()
    val imagePickerLauncher = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = pickerScope,
        onResult = { byteArrays -> byteArrays.firstOrNull()?.let(viewModel::onPhotoPicked) }
    )

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (uiState.isEditing) "Editar cabra" else "Nueva cabra") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChanged,
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = uiState.tagNumber,
                    onValueChange = viewModel::onTagNumberChanged,
                    label = { Text("Número de arete") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                Column {
                    Text("Sexo")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = uiState.sex == GoatSex.FEMALE,
                            onClick = { viewModel.onSexChanged(GoatSex.FEMALE) },
                            label = { Text("Hembra") }
                        )
                        FilterChip(
                            selected = uiState.sex == GoatSex.MALE,
                            onClick = { viewModel.onSexChanged(GoatSex.MALE) },
                            label = { Text("Macho") }
                        )
                    }
                }
            }
            item {
                Column {
                    Text("Composición racial (opcional)")
                    Text(
                        "100% de una raza o una mezcla. Si nace en la finca y eliges madre y padre " +
                            "del hato, se calcula sola — igual puedes ajustarla a mano.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    uiState.breedRows.forEach { row ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = row.breedName,
                                onValueChange = { viewModel.onBreedRowNameChanged(row.rowId, it) },
                                label = { Text("Raza") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = row.percentageText,
                                onValueChange = { viewModel.onBreedRowPercentageChanged(row.rowId, it) },
                                label = { Text("%") },
                                modifier = Modifier.width(90.dp),
                                singleLine = true
                            )
                            IconButton(onClick = { viewModel.onRemoveBreedRow(row.rowId) }) {
                                Icon(Icons.Outlined.Close, contentDescription = "Quitar raza")
                            }
                        }
                    }
                    TextButton(onClick = viewModel::onAddBreedRow) {
                        Icon(Icons.Outlined.Add, contentDescription = null)
                        Text(" Agregar raza")
                    }
                    if (uiState.breedRows.isNotEmpty()) {
                        Text(
                            "Total: ${formatQuantity(uiState.breedCompositionTotal)}%",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            item {
                DateField(
                    label = "Fecha de nacimiento",
                    date = uiState.birthDate,
                    onDateSelected = viewModel::onBirthDateChanged
                )
            }
            item {
                DateField(
                    label = "Fecha de destete (opcional)",
                    date = uiState.weaningDate,
                    onDateSelected = viewModel::onWeaningDateChanged
                )
            }
            item {
                Column {
                    Text("Madre")
                    OutlinedButton(onClick = { showMotherPicker = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(uiState.mother?.let { "${it.name} (arete ${it.tagNumber})" } ?: "Seleccionar / desconocida")
                    }
                }
            }
            item {
                Column {
                    Text("Padre")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !fatherIsExternal,
                            onClick = { fatherIsExternal = false; viewModel.onExternalFatherDescriptionChanged("") },
                            label = { Text("Del hato") }
                        )
                        FilterChip(
                            selected = fatherIsExternal,
                            onClick = { fatherIsExternal = true; viewModel.onFatherSelected(null) },
                            label = { Text("Semental externo") }
                        )
                    }
                    if (fatherIsExternal) {
                        OutlinedTextField(
                            value = uiState.externalFatherDescription,
                            onValueChange = viewModel::onExternalFatherDescriptionChanged,
                            label = { Text("Descripción del semental externo") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        OutlinedButton(onClick = { showFatherPicker = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(uiState.father?.let { "${it.name} (arete ${it.tagNumber})" } ?: "Seleccionar / desconocido")
                        }
                    }
                }
            }
            item {
                Column {
                    Text("Origen")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = uiState.origin == GoatOrigin.BORN_ON_FARM,
                            onClick = { viewModel.onOriginChanged(GoatOrigin.BORN_ON_FARM) },
                            label = { Text("Nacida en la finca") }
                        )
                        FilterChip(
                            selected = uiState.origin == GoatOrigin.PURCHASED,
                            onClick = { viewModel.onOriginChanged(GoatOrigin.PURCHASED) },
                            label = { Text("Comprada") }
                        )
                    }
                }
            }
            item {
                Column {
                    Text("Foto (opcional)")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val bytes = uiState.photoBytes
                        when {
                            bytes != null -> {
                                val bitmap = remember(bytes) { bytes.toImageBitmap() }
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(64.dp).clip(CircleShape)
                                )
                            }
                            uiState.existingPhotoUrl != null && !uiState.photoRemoved -> {
                                AsyncImage(
                                    model = uiState.existingPhotoUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(64.dp).clip(CircleShape)
                                )
                            }
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                            }
                        }
                        OutlinedButton(onClick = { imagePickerLauncher.launch() }) {
                            Text(if (uiState.hasPhoto) "Cambiar foto" else "Elegir foto")
                        }
                        if (uiState.hasPhoto) {
                            TextButton(onClick = viewModel::onPhotoCleared) { Text("Quitar") }
                        }
                    }
                }
            }
            if (uiState.requiresInitialStatus) {
                item {
                    Column {
                        Text("Estado inicial (requerido para compras de cabras adultas)")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(
                                listOf(GoatStatus.IN_PRODUCTION, GoatStatus.PREGNANT, GoatStatus.DRY, GoatStatus.BREEDING_BUCK)
                            ) { status ->
                                FilterChip(
                                    selected = uiState.initialStatus == status,
                                    onClick = { viewModel.onInitialStatusChanged(status) },
                                    label = { Text(status.label()) }
                                )
                            }
                        }
                    }
                }
            }
            if (uiState.isEditing) {
                item {
                    Column {
                        Text("Estado actual")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(GoatStatus.entries) { status ->
                                FilterChip(
                                    selected = uiState.currentStatus == status,
                                    onClick = { viewModel.onCurrentStatusChanged(status) },
                                    label = { Text(status.label()) }
                                )
                            }
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().weight(1f)) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = { viewModel.save(onSaved) },
                        enabled = uiState.isValid && !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.fillMaxWidth())
                        } else {
                            Text("Guardar")
                        }
                    }
                }
            }
        }
    }

    if (showMotherPicker) {
        GoatPickerDialog(
            title = "Seleccionar madre",
            candidates = uiState.availableMothers,
            onDismiss = { showMotherPicker = false },
            onSelect = { goat -> viewModel.onMotherSelected(goat); showMotherPicker = false }
        )
    }
    if (showFatherPicker) {
        GoatPickerDialog(
            title = "Seleccionar padre",
            candidates = uiState.availableFathers,
            onDismiss = { showFatherPicker = false },
            onSelect = { goat -> viewModel.onFatherSelected(goat); showFatherPicker = false }
        )
    }
}
