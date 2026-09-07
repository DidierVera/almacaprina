package com.didiprogrammer.almacaprina.ui.admin.hato

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.didiprogrammer.almacaprina.business.breedCompositionLabel
import com.didiprogrammer.almacaprina.domain.model.BreedPercentage
import com.didiprogrammer.almacaprina.domain.model.GoatOrigin
import com.didiprogrammer.almacaprina.domain.model.GoatSex
import com.didiprogrammer.almacaprina.domain.model.HealthRecord
import com.didiprogrammer.almacaprina.domain.model.ReproductiveEvent
import com.didiprogrammer.almacaprina.domain.model.ReproductiveEventResult
import com.didiprogrammer.almacaprina.domain.model.WeightRecord
import com.didiprogrammer.almacaprina.ui.components.AlmacaprinaCard
import com.didiprogrammer.almacaprina.ui.components.GoatAvatar
import com.didiprogrammer.almacaprina.ui.components.GoatStatusChip
import com.didiprogrammer.almacaprina.ui.components.SectionHeader
import com.didiprogrammer.almacaprina.ui.components.SimpleLineChart
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Sección 2, pantalla 2.2 — Ficha técnica de la cabra.
 */
@Composable
fun AdminGoatDetailScreen(
    goatId: String,
    onBack: () -> Unit,
    onNavigateToGoat: (String) -> Unit,
    onEditClick: () -> Unit,
    viewModel: AdminGoatDetailViewModel = koinViewModel(parameters = { parametersOf(goatId) })
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    com.didiprogrammer.almacaprina.ui.components.RefreshOnResume(viewModel::load)
    var showWeightDialog by remember { mutableStateOf(false) }
    var showReproEventDialog by remember { mutableStateOf(false) }
    var showHealthEventDialog by remember { mutableStateOf(false) }

    if (uiState.isLoading || uiState.goat == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (uiState.errorMessage != null) {
                Text(uiState.errorMessage ?: "")
            } else {
                CircularProgressIndicator()
            }
        }
        return
    }

    val goat = uiState.goat!!
    val tabs = buildList {
        add(GoatDetailTab.DATOS_BASICOS)
        add(GoatDetailTab.PESO)
        add(GoatDetailTab.REPRODUCCION)
        add(GoatDetailTab.SALUD)
        if (uiState.hasMilkHistory) add(GoatDetailTab.PRODUCCION_LECHE)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(goat.name) },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar ficha")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            when (uiState.selectedTab) {
                GoatDetailTab.PESO -> ExtendedFloatingActionButton(onClick = { showWeightDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text(" Registrar pesada")
                }
                GoatDetailTab.REPRODUCCION -> ExtendedFloatingActionButton(onClick = { showReproEventDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text(" Registrar evento")
                }
                GoatDetailTab.SALUD -> ExtendedFloatingActionButton(onClick = { showHealthEventDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text(" Registrar evento")
                }
                else -> {}
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            GoatDetailHeader(
                name = goat.name,
                photoUrl = goat.photoUrl,
                tagNumber = goat.tagNumber,
                ageLabel = uiState.ageLabel,
                statusChip = { GoatStatusChip(status = goat.currentStatus) }
            )

            PrimaryScrollableTabRow(selectedTabIndex = tabs.indexOf(uiState.selectedTab).coerceAtLeast(0)) {
                tabs.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.onTabSelected(tab) },
                        text = { Text(tab.label) }
                    )
                }
            }

            when (uiState.selectedTab) {
                GoatDetailTab.DATOS_BASICOS -> DatosBasicosTab(
                    breedComposition = goat.breedComposition,
                    birthDate = goat.birthDate.toString(),
                    weaningDate = goat.weaningDate?.toString(),
                    origin = goat.origin,
                    sex = goat.sex,
                    motherName = uiState.motherName,
                    onMotherClick = goat.motherId?.let { id -> { onNavigateToGoat(id) } },
                    fatherName = uiState.fatherName,
                    onFatherClick = goat.fatherId?.let { id -> { onNavigateToGoat(id) } },
                    lactationNumber = uiState.lactationNumber,
                    notes = goat.notes
                )
                GoatDetailTab.PESO -> PesoTab(
                    weightRecords = uiState.weightRecords,
                    currentBcs = goat.currentBodyConditionScore
                )
                GoatDetailTab.REPRODUCCION -> ReproduccionTab(
                    events = uiState.reproductiveEvents,
                    nextExpectedBirth = uiState.nextExpectedBirth
                )
                GoatDetailTab.SALUD -> SaludTab(records = uiState.healthRecords)
                GoatDetailTab.PRODUCCION_LECHE -> ProduccionLecheTab(records = uiState.milkRecords)
            }
        }
    }

    if (showWeightDialog) {
        RegisterWeightDialog(
            onDismiss = { showWeightDialog = false },
            onSave = { date, weightKg, bcs, notes ->
                viewModel.onSaveWeight(date, weightKg, bcs, notes)
                showWeightDialog = false
            }
        )
    }
    if (showReproEventDialog) {
        RegisterReproductiveEventDialog(
            bucks = uiState.availableBucks,
            onDismiss = { showReproEventDialog = false },
            onSave = { form ->
                viewModel.onSaveReproductiveEvent(form)
                showReproEventDialog = false
            }
        )
    }
    if (showHealthEventDialog) {
        RegisterHealthEventDialog(
            veterinaryInsumos = uiState.veterinaryInsumos,
            onDismiss = { showHealthEventDialog = false },
            onSave = { form ->
                viewModel.onSaveHealthEvent(form)
                showHealthEventDialog = false
            }
        )
    }
}

@Composable
private fun GoatDetailHeader(
    name: String,
    photoUrl: String?,
    tagNumber: String,
    ageLabel: String,
    statusChip: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GoatAvatar(name = name, photoUrl = photoUrl, size = 64.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleLarge)
            Text("Arete $tagNumber · $ageLabel", style = MaterialTheme.typography.bodyMedium)
        }
        statusChip()
    }
}

@Composable
private fun DatosBasicosTab(
    breedComposition: List<BreedPercentage>,
    birthDate: String,
    weaningDate: String?,
    origin: GoatOrigin,
    sex: GoatSex,
    motherName: String?,
    onMotherClick: (() -> Unit)?,
    fatherName: String?,
    onFatherClick: (() -> Unit)?,
    lactationNumber: Int,
    notes: String?
) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            AlmacaprinaCard {
                InfoRow("Sexo", if (sex == GoatSex.FEMALE) "Hembra" else "Macho")
                InfoRow("Composición racial", breedCompositionLabel(breedComposition))
                InfoRow("Nacimiento", birthDate)
                if (weaningDate != null) InfoRow("Destete", weaningDate)
                InfoRow("Origen", if (origin == GoatOrigin.BORN_ON_FARM) "Nacida en la finca" else "Comprada")
                if (lactationNumber > 0) InfoRow("Lactancia", "$lactationNumber°")
            }
        }
        item {
            AlmacaprinaCard {
                Text("Padres", style = MaterialTheme.typography.titleSmall)
                ParentRow(label = "Madre", name = motherName, onClick = onMotherClick)
                ParentRow(label = "Padre", name = fatherName, onClick = onFatherClick)
            }
        }
        if (!notes.isNullOrBlank()) {
            item {
                AlmacaprinaCard {
                    Text("Notas", style = MaterialTheme.typography.titleSmall)
                    Text(notes, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun ParentRow(label: String, name: String?, onClick: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(vertical = 6.dp)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = name ?: "Sin registrar",
            style = MaterialTheme.typography.bodyMedium,
            color = if (onClick != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun PesoTab(weightRecords: List<WeightRecord>, currentBcs: Int?) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            AlmacaprinaCard {
                val last = weightRecords.lastOrNull()
                Text("Último peso", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = last?.let { "${it.weightKg} kg" } ?: "Sin registros",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "BCS: ${currentBcs ?: last?.bodyConditionScore ?: "sin dato"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (weightRecords.size >= 2) {
                    SectionHeader(title = "Evolución", modifier = Modifier.padding(top = 12.dp))
                    SimpleLineChart(values = weightRecords.map { it.weightKg.toFloat() })
                }
            }
        }
        items(weightRecords.sortedByDescending { it.date }) { record ->
            AlmacaprinaCard {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(record.date.toString(), style = MaterialTheme.typography.bodyMedium)
                    Text("${record.weightKg} kg", style = MaterialTheme.typography.bodyMedium)
                }
                record.notes?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

@Composable
private fun ReproduccionTab(events: List<ReproductiveEvent>, nextExpectedBirth: kotlinx.datetime.LocalDate?) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (nextExpectedBirth != null) {
            item {
                AlmacaprinaCard {
                    Text("Próximo parto estimado", style = MaterialTheme.typography.titleSmall)
                    Text(nextExpectedBirth.toString(), style = MaterialTheme.typography.titleLarge)
                }
            }
        }
        if (events.isEmpty()) {
            item { Text("Sin eventos reproductivos registrados.", style = MaterialTheme.typography.bodyMedium) }
        }
        items(events) { event ->
            AlmacaprinaCard {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(event.eventType.name, style = MaterialTheme.typography.titleSmall)
                    Text(event.date.toString(), style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    text = "Resultado: ${event.result.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (event.result == ReproductiveEventResult.SUCCESSFUL) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                )
                event.notes?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

@Composable
private fun SaludTab(records: List<HealthRecord>) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (records.isEmpty()) {
            item { Text("Sin historial de salud registrado.", style = MaterialTheme.typography.bodyMedium) }
        }
        items(records) { record ->
            AlmacaprinaCard {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(record.type.name, style = MaterialTheme.typography.titleSmall)
                    Text(record.date.toString(), style = MaterialTheme.typography.bodySmall)
                }
                record.description?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                record.nextSuggestedDate?.let {
                    Text(
                        "Próxima fecha sugerida: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun ProduccionLecheTab(records: List<com.didiprogrammer.almacaprina.domain.model.MilkProductionRecord>) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            AlmacaprinaCard {
                Text("Curva de litros", style = MaterialTheme.typography.titleSmall)
                SimpleLineChart(
                    values = records.map {
                        ((it.morningMilkingLiters ?: 0.0) + (it.eveningMilkingLiters ?: 0.0)).toFloat()
                    }
                )
            }
        }
    }
}
