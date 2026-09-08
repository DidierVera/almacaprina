package com.didiprogrammer.almacaprina.ui.admin.hato

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_goat_detail_father_label
import almacaprina.shared.generated.resources.admin_goat_detail_mother_label
import almacaprina.shared.generated.resources.admin_goat_detail_origin_born
import almacaprina.shared.generated.resources.admin_goat_detail_origin_label
import almacaprina.shared.generated.resources.admin_goat_detail_origin_purchased
import almacaprina.shared.generated.resources.admin_goat_detail_sex_female
import almacaprina.shared.generated.resources.admin_goat_detail_sex_label
import almacaprina.shared.generated.resources.admin_goat_detail_sex_male
import almacaprina.shared.generated.resources.admin_goat_form_add_breed_button
import almacaprina.shared.generated.resources.admin_goat_form_birth_date_label
import almacaprina.shared.generated.resources.admin_goat_form_breed_composition_hint
import almacaprina.shared.generated.resources.admin_goat_form_breed_composition_label
import almacaprina.shared.generated.resources.admin_goat_form_breed_select_placeholder
import almacaprina.shared.generated.resources.admin_goat_form_breed_total_label
import almacaprina.shared.generated.resources.admin_goat_form_change_photo_button
import almacaprina.shared.generated.resources.admin_goat_form_choose_photo_button
import almacaprina.shared.generated.resources.admin_goat_form_current_status_label
import almacaprina.shared.generated.resources.admin_goat_form_edit_title
import almacaprina.shared.generated.resources.admin_goat_form_external_father_description_label
import almacaprina.shared.generated.resources.admin_goat_form_father_external_label
import almacaprina.shared.generated.resources.admin_goat_form_father_internal_label
import almacaprina.shared.generated.resources.admin_goat_form_goat_with_tag
import almacaprina.shared.generated.resources.admin_goat_form_initial_status_label
import almacaprina.shared.generated.resources.admin_goat_form_name_label
import almacaprina.shared.generated.resources.admin_goat_form_new_title
import almacaprina.shared.generated.resources.admin_goat_form_photo_label
import almacaprina.shared.generated.resources.admin_goat_form_pick_father_title
import almacaprina.shared.generated.resources.admin_goat_form_pick_mother_title
import almacaprina.shared.generated.resources.admin_goat_form_remove_breed_content_description
import almacaprina.shared.generated.resources.admin_goat_form_remove_photo_button
import almacaprina.shared.generated.resources.admin_goat_form_select_unknown_female
import almacaprina.shared.generated.resources.admin_goat_form_select_unknown_male
import almacaprina.shared.generated.resources.admin_goat_form_tag_number_label
import almacaprina.shared.generated.resources.admin_goat_form_weaning_date_label
import almacaprina.shared.generated.resources.common_cancel
import almacaprina.shared.generated.resources.common_save_button
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
import com.didiprogrammer.almacaprina.ui.components.GoatPickerDialog
import com.didiprogrammer.almacaprina.ui.components.label
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.image.picker.toImageBitmap
import org.jetbrains.compose.resources.stringResource
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
    var breedPickerRowId by remember { mutableStateOf<String?>(null) }
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
        topBar = { TopAppBar(title = { Text(stringResource(if (uiState.isEditing) Res.string.admin_goat_form_edit_title else Res.string.admin_goat_form_new_title)) }) },
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
                    label = { Text(stringResource(Res.string.admin_goat_form_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = uiState.tagNumber,
                    onValueChange = viewModel::onTagNumberChanged,
                    label = { Text(stringResource(Res.string.admin_goat_form_tag_number_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                Column {
                    Text(stringResource(Res.string.admin_goat_detail_sex_label))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = uiState.sex == GoatSex.FEMALE,
                            onClick = { viewModel.onSexChanged(GoatSex.FEMALE) },
                            label = { Text(stringResource(Res.string.admin_goat_detail_sex_female)) }
                        )
                        FilterChip(
                            selected = uiState.sex == GoatSex.MALE,
                            onClick = { viewModel.onSexChanged(GoatSex.MALE) },
                            label = { Text(stringResource(Res.string.admin_goat_detail_sex_male)) }
                        )
                    }
                }
            }
            item {
                Column {
                    Text(stringResource(Res.string.admin_goat_form_breed_composition_label))
                    Text(
                        stringResource(Res.string.admin_goat_form_breed_composition_hint),
                        style = MaterialTheme.typography.bodySmall
                    )
                    uiState.breedRows.forEach { row ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { breedPickerRowId = row.rowId },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(row.breedName.ifBlank { stringResource(Res.string.admin_goat_form_breed_select_placeholder) })
                            }
                            OutlinedTextField(
                                value = row.percentageText,
                                onValueChange = { viewModel.onBreedRowPercentageChanged(row.rowId, it) },
                                label = { Text("%") },
                                modifier = Modifier.width(90.dp),
                                singleLine = true
                            )
                            IconButton(onClick = { viewModel.onRemoveBreedRow(row.rowId) }) {
                                Icon(Icons.Outlined.Close, contentDescription = stringResource(Res.string.admin_goat_form_remove_breed_content_description))
                            }
                        }
                    }
                    TextButton(onClick = viewModel::onAddBreedRow) {
                        Icon(Icons.Outlined.Add, contentDescription = null)
                        Text(" " + stringResource(Res.string.admin_goat_form_add_breed_button))
                    }
                    if (uiState.breedRows.isNotEmpty()) {
                        Text(
                            stringResource(Res.string.admin_goat_form_breed_total_label, formatQuantity(uiState.breedCompositionTotal)),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            item {
                DateField(
                    label = stringResource(Res.string.admin_goat_form_birth_date_label),
                    date = uiState.birthDate,
                    onDateSelected = viewModel::onBirthDateChanged
                )
            }
            item {
                DateField(
                    label = stringResource(Res.string.admin_goat_form_weaning_date_label),
                    date = uiState.weaningDate,
                    onDateSelected = viewModel::onWeaningDateChanged
                )
            }
            item {
                Column {
                    Text(stringResource(Res.string.admin_goat_detail_mother_label))
                    OutlinedButton(onClick = { showMotherPicker = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(uiState.mother?.let { stringResource(Res.string.admin_goat_form_goat_with_tag, it.name, it.tagNumber) } ?: stringResource(Res.string.admin_goat_form_select_unknown_female))
                    }
                }
            }
            item {
                Column {
                    Text(stringResource(Res.string.admin_goat_detail_father_label))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !fatherIsExternal,
                            onClick = { fatherIsExternal = false; viewModel.onExternalFatherDescriptionChanged("") },
                            label = { Text(stringResource(Res.string.admin_goat_form_father_internal_label)) }
                        )
                        FilterChip(
                            selected = fatherIsExternal,
                            onClick = { fatherIsExternal = true; viewModel.onFatherSelected(null) },
                            label = { Text(stringResource(Res.string.admin_goat_form_father_external_label)) }
                        )
                    }
                    if (fatherIsExternal) {
                        OutlinedTextField(
                            value = uiState.externalFatherDescription,
                            onValueChange = viewModel::onExternalFatherDescriptionChanged,
                            label = { Text(stringResource(Res.string.admin_goat_form_external_father_description_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        OutlinedButton(onClick = { showFatherPicker = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(uiState.father?.let { stringResource(Res.string.admin_goat_form_goat_with_tag, it.name, it.tagNumber) } ?: stringResource(Res.string.admin_goat_form_select_unknown_male))
                        }
                    }
                }
            }
            item {
                Column {
                    Text(stringResource(Res.string.admin_goat_detail_origin_label))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = uiState.origin == GoatOrigin.BORN_ON_FARM,
                            onClick = { viewModel.onOriginChanged(GoatOrigin.BORN_ON_FARM) },
                            label = { Text(stringResource(Res.string.admin_goat_detail_origin_born)) }
                        )
                        FilterChip(
                            selected = uiState.origin == GoatOrigin.PURCHASED,
                            onClick = { viewModel.onOriginChanged(GoatOrigin.PURCHASED) },
                            label = { Text(stringResource(Res.string.admin_goat_detail_origin_purchased)) }
                        )
                    }
                }
            }
            item {
                Column {
                    Text(stringResource(Res.string.admin_goat_form_photo_label))
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
                            Text(stringResource(if (uiState.hasPhoto) Res.string.admin_goat_form_change_photo_button else Res.string.admin_goat_form_choose_photo_button))
                        }
                        if (uiState.hasPhoto) {
                            TextButton(onClick = viewModel::onPhotoCleared) { Text(stringResource(Res.string.admin_goat_form_remove_photo_button)) }
                        }
                    }
                }
            }
            if (uiState.requiresInitialStatus) {
                item {
                    Column {
                        Text(stringResource(Res.string.admin_goat_form_initial_status_label))
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
                        Text(stringResource(Res.string.admin_goat_form_current_status_label))
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
                        Text(stringResource(Res.string.common_cancel))
                    }
                    Button(
                        onClick = { viewModel.save(onSaved) },
                        enabled = uiState.isValid && !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.fillMaxWidth())
                        } else {
                            Text(stringResource(Res.string.common_save_button))
                        }
                    }
                }
            }
        }
    }

    if (showMotherPicker) {
        GoatPickerDialog(
            title = stringResource(Res.string.admin_goat_form_pick_mother_title),
            candidates = uiState.availableMothers,
            onDismiss = { showMotherPicker = false },
            onSelect = { goat -> viewModel.onMotherSelected(goat); showMotherPicker = false }
        )
    }
    if (showFatherPicker) {
        GoatPickerDialog(
            title = stringResource(Res.string.admin_goat_form_pick_father_title),
            candidates = uiState.availableFathers,
            onDismiss = { showFatherPicker = false },
            onSelect = { goat -> viewModel.onFatherSelected(goat); showFatherPicker = false }
        )
    }
    breedPickerRowId?.let { rowId ->
        BreedPickerDialog(
            breeds = uiState.breeds,
            onDismiss = { breedPickerRowId = null },
            onSelect = { breed -> viewModel.onBreedRowNameChanged(rowId, breed.name); breedPickerRowId = null },
            onCreateNew = { name -> viewModel.createAndSelectBreed(rowId, name); breedPickerRowId = null }
        )
    }
}
