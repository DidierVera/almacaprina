package com.didiprogrammer.almacaprina.ui.admin.hato

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_goat_form_error_save
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.business.averageBreedComposition
import com.didiprogrammer.almacaprina.business.formatQuantity
import com.didiprogrammer.almacaprina.data.remote.PhotoUploadService
import com.didiprogrammer.almacaprina.domain.model.Breed
import com.didiprogrammer.almacaprina.domain.model.BreedPercentage
import com.didiprogrammer.almacaprina.domain.model.Goat
import com.didiprogrammer.almacaprina.domain.model.GoatOrigin
import com.didiprogrammer.almacaprina.domain.model.GoatSex
import com.didiprogrammer.almacaprina.domain.model.GoatStatus
import com.didiprogrammer.almacaprina.domain.repository.BreedRepository
import com.didiprogrammer.almacaprina.domain.repository.GoatRepository
import com.didiprogrammer.almacaprina.util.newId
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import org.jetbrains.compose.resources.getString

/**
 * Sección 2, pantalla 2.3 — Alta y edición de una cabra en un mismo formulario.
 * `goatId` nulo = alta.
 */
class AdminGoatFormViewModel(
    private val goatId: String?,
    private val goatRepository: GoatRepository,
    private val breedRepository: BreedRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminGoatFormUiState(editingGoatId = goatId))
    val uiState: StateFlow<AdminGoatFormUiState> = _uiState.asStateFlow()

    // Se preserva el resto de campos del registro original (herd_entry_date, exit_date,
    // exit_reason...) que no son parte de este formulario, vía .copy() al guardar.
    private var existingGoat: Goat? = null

    init {
        viewModelScope.launch {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val (allGoats, breeds) = coroutineScope {
                val allGoatsDeferred = async { goatRepository.getAll() }
                val breedsDeferred = async { breedRepository.getAll() }
                allGoatsDeferred.await() to breedsDeferred.await()
            }
            val existing = goatId?.let { id -> allGoats.firstOrNull { it.id == id } ?: goatRepository.getById(id) }
            existingGoat = existing

            if (existing != null) {
                val mother = existing.motherId?.let { id -> allGoats.firstOrNull { it.id == id } }
                val father = existing.fatherId?.let { id -> allGoats.firstOrNull { it.id == id } }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allGoats = allGoats,
                        today = today,
                        breeds = breeds,
                        name = existing.name,
                        tagNumber = existing.tagNumber,
                        sex = existing.sex,
                        breedRows = existing.breedComposition.toRows(),
                        birthDate = existing.birthDate,
                        weaningDate = existing.weaningDate,
                        mother = mother,
                        father = father,
                        externalFatherDescription = existing.externalFatherDescription ?: "",
                        origin = existing.origin,
                        existingPhotoUrl = existing.photoUrl,
                        currentStatus = existing.currentStatus
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, allGoats = allGoats, today = today, breeds = breeds) }
            }
        }
    }

    fun onNameChanged(value: String) = _uiState.update { it.copy(name = value) }
    fun onTagNumberChanged(value: String) = _uiState.update { it.copy(tagNumber = value) }
    fun onSexChanged(value: GoatSex) = _uiState.update { it.copy(sex = value) }
    fun onBirthDateChanged(value: LocalDate) = _uiState.update { it.copy(birthDate = value) }
    fun onWeaningDateChanged(value: LocalDate) = _uiState.update { it.copy(weaningDate = value) }

    fun onMotherSelected(value: Goat?) {
        _uiState.update { it.copy(mother = value) }
        autoCalculateBreedComposition()
    }

    fun onFatherSelected(value: Goat?) {
        _uiState.update { it.copy(father = value, externalFatherDescription = "") }
        autoCalculateBreedComposition()
    }

    fun onExternalFatherDescriptionChanged(value: String) = _uiState.update { it.copy(externalFatherDescription = value, father = null) }
    fun onOriginChanged(value: GoatOrigin) = _uiState.update { it.copy(origin = value, initialStatus = null) }
    fun onPhotoPicked(bytes: ByteArray) = _uiState.update { it.copy(photoBytes = bytes, photoRemoved = false) }
    fun onPhotoCleared() = _uiState.update { it.copy(photoBytes = null, photoRemoved = true) }
    fun onInitialStatusChanged(value: GoatStatus) = _uiState.update { it.copy(initialStatus = value) }
    fun onCurrentStatusChanged(value: GoatStatus) = _uiState.update { it.copy(currentStatus = value) }

    fun onAddBreedRow() = _uiState.update { it.copy(breedRows = it.breedRows + BreedCompositionRow()) }

    fun onRemoveBreedRow(rowId: String) = _uiState.update { it.copy(breedRows = it.breedRows.filterNot { row -> row.rowId == rowId }) }

    fun onBreedRowNameChanged(rowId: String, name: String) = _uiState.update { state ->
        state.copy(breedRows = state.breedRows.map { row -> if (row.rowId == rowId) row.copy(breedName = name) else row })
    }

    fun onBreedRowPercentageChanged(rowId: String, percentageText: String) = _uiState.update { state ->
        state.copy(breedRows = state.breedRows.map { row -> if (row.rowId == rowId) row.copy(percentageText = percentageText) else row })
    }

    /** Crea una raza nueva en el maestro de razas (Catálogo) y la asigna de una vez a la fila
     * que abrió el selector — evita salir del formulario para dar de alta una raza nueva. */
    fun createAndSelectBreed(rowId: String, name: String) {
        viewModelScope.launch {
            try {
                val breed = breedRepository.insert(Breed(id = newId(), name = name))
                _uiState.update { state ->
                    state.copy(
                        breeds = state.breeds + breed,
                        breedRows = state.breedRows.map { row -> if (row.rowId == rowId) row.copy(breedName = breed.name) else row }
                    )
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(errorMessage = t.message ?: getString(Res.string.admin_goat_form_error_save)) }
            }
        }
    }

    /**
     * Si la cría nace en la finca y ya se conocen ambos padres del hato, se calcula
     * automáticamente la composición racial como el promedio de la de cada uno (ver
     * CLAUDE.md / business.averageBreedComposition). Solo se aplica cuando el editor
     * todavía está vacío, para no pisar una composición ya cargada o editada a mano.
     */
    private fun autoCalculateBreedComposition() {
        val state = _uiState.value
        if (state.origin != GoatOrigin.BORN_ON_FARM) return
        if (state.breedComposition.isNotEmpty()) return
        val mother = state.mother ?: return
        val father = state.father
        val calculated = averageBreedComposition(mother.breedComposition, father?.breedComposition ?: emptyList())
        if (calculated.isNotEmpty()) {
            _uiState.update { it.copy(breedRows = calculated.toRows()) }
        }
    }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        if (!state.isValid) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val today = state.today ?: Clock.System.todayIn(TimeZone.currentSystemDefault())
                val id = state.editingGoatId ?: newId()

                val photoUrl: String? = when {
                    state.photoBytes != null -> {
                        _uiState.update { it.copy(isUploadingPhoto = true) }
                        val url = PhotoUploadService.uploadGoatPhoto(id, state.photoBytes)
                        _uiState.update { it.copy(isUploadingPhoto = false) }
                        url
                    }
                    state.photoRemoved -> null
                    else -> state.existingPhotoUrl
                }

                val base = existingGoat
                if (base != null) {
                    // Edición: se preservan herd_entry_date, exit_date, exit_reason, etc.
                    goatRepository.update(
                        id,
                        base.copy(
                            tagNumber = state.tagNumber.trim(),
                            name = state.name.trim(),
                            photoUrl = photoUrl,
                            sex = state.sex,
                            breedComposition = state.breedComposition,
                            birthDate = state.birthDate!!,
                            weaningDate = state.weaningDate,
                            motherId = state.mother?.id,
                            fatherId = state.father?.id,
                            externalFatherDescription = state.externalFatherDescription.ifBlank { null },
                            currentStatus = state.currentStatus ?: base.currentStatus,
                            origin = state.origin
                        )
                    )
                } else {
                    // Alta: fuera de una compra de adulta (con estado inicial explícito), el estado
                    // debería derivar de eventos reproductivos — como no hay historial previo, se
                    // asume "cabrito" por defecto (ver notas de la sección).
                    goatRepository.insert(
                        Goat(
                            id = id,
                            tagNumber = state.tagNumber.trim(),
                            name = state.name.trim(),
                            photoUrl = photoUrl,
                            sex = state.sex,
                            breedComposition = state.breedComposition,
                            birthDate = state.birthDate!!,
                            weaningDate = state.weaningDate,
                            motherId = state.mother?.id,
                            fatherId = state.father?.id,
                            externalFatherDescription = state.externalFatherDescription.ifBlank { null },
                            currentStatus = state.initialStatus ?: GoatStatus.KID,
                            herdEntryDate = today,
                            origin = state.origin
                        )
                    )
                }
                onSaved()
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isSaving = false, isUploadingPhoto = false, errorMessage = t.message ?: getString(Res.string.admin_goat_form_error_save)) }
            }
        }
    }
}

private fun List<BreedPercentage>.toRows(): List<BreedCompositionRow> =
    map { BreedCompositionRow(breedName = it.breedName, percentageText = formatQuantity(it.percentage)) }
