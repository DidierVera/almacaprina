package com.didiprogrammer.almacaprina.ui.admin.hato

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.business.ageLabel
import com.didiprogrammer.almacaprina.business.averageBreedComposition
import com.didiprogrammer.almacaprina.business.expectedBirthDateOrNull
import com.didiprogrammer.almacaprina.business.lactationNumber
import com.didiprogrammer.almacaprina.domain.model.Goat
import com.didiprogrammer.almacaprina.domain.model.GoatOrigin
import com.didiprogrammer.almacaprina.domain.model.GoatSex
import com.didiprogrammer.almacaprina.domain.model.GoatStatus
import com.didiprogrammer.almacaprina.domain.model.HealthRecord
import com.didiprogrammer.almacaprina.domain.model.Insumo
import com.didiprogrammer.almacaprina.domain.model.InsumoCategory
import com.didiprogrammer.almacaprina.domain.model.MilkProductionRecord
import com.didiprogrammer.almacaprina.domain.model.ReproductiveEvent
import com.didiprogrammer.almacaprina.domain.model.ReproductiveEventResult
import com.didiprogrammer.almacaprina.domain.model.ReproductiveEventType
import com.didiprogrammer.almacaprina.domain.model.WeightRecord
import com.didiprogrammer.almacaprina.domain.repository.GoatRepository
import com.didiprogrammer.almacaprina.domain.repository.HealthRecordRepository
import com.didiprogrammer.almacaprina.domain.repository.InsumoRepository
import com.didiprogrammer.almacaprina.domain.repository.MilkProductionRecordRepository
import com.didiprogrammer.almacaprina.domain.repository.ReproductiveEventRepository
import com.didiprogrammer.almacaprina.domain.repository.WeightRecordRepository
import com.didiprogrammer.almacaprina.util.newId
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/** Bundle de los datos crudos de la ficha — se piden todos en paralelo, ver [AdminGoatDetailViewModel.load]. */
private data class AdminGoatDetailRawData(
    val goat: Goat?,
    val allGoats: List<Goat>,
    val weightRecords: List<WeightRecord>,
    val reproductiveEvents: List<ReproductiveEvent>,
    val healthRecords: List<HealthRecord>,
    val milkRecords: List<MilkProductionRecord>,
    val insumos: List<Insumo>
)

class AdminGoatDetailViewModel(
    private val goatId: String,
    private val goatRepository: GoatRepository,
    private val weightRecordRepository: WeightRecordRepository,
    private val reproductiveEventRepository: ReproductiveEventRepository,
    private val healthRecordRepository: HealthRecordRepository,
    private val milkProductionRecordRepository: MilkProductionRecordRepository,
    private val insumoRepository: InsumoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminGoatDetailUiState())
    val uiState: StateFlow<AdminGoatDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

                // Ninguna de estas consultas depende del resultado de otra (todas
                // filtran por el goatId ya conocido) — se piden todas a la vez.
                val (goat, allGoats, weightRecordsRaw, reproductiveEventsRaw, healthRecordsRaw, milkRecordsRaw, insumos) = coroutineScope {
                    val goatDeferred = async { goatRepository.getById(goatId) }
                    val allGoatsDeferred = async { goatRepository.getAll() }
                    val weightRecordsDeferred = async { weightRecordRepository.getAll() }
                    val reproductiveEventsDeferred = async { reproductiveEventRepository.getAll() }
                    val healthRecordsDeferred = async { healthRecordRepository.getAll() }
                    val milkRecordsDeferred = async { milkProductionRecordRepository.getAll() }
                    val insumosDeferred = async { insumoRepository.getAll() }

                    AdminGoatDetailRawData(
                        goat = goatDeferred.await(),
                        allGoats = allGoatsDeferred.await(),
                        weightRecords = weightRecordsDeferred.await(),
                        reproductiveEvents = reproductiveEventsDeferred.await(),
                        healthRecords = healthRecordsDeferred.await(),
                        milkRecords = milkRecordsDeferred.await(),
                        insumos = insumosDeferred.await()
                    )
                }

                if (goat == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "No se encontró la cabra") }
                    return@launch
                }
                val mother = goat.motherId?.let { id -> allGoats.firstOrNull { it.id == id } }
                val father = goat.fatherId?.let { id -> allGoats.firstOrNull { it.id == id } }

                val weightRecords = weightRecordsRaw
                    .filter { it.goatId == goatId }
                    .sortedBy { it.date }

                val reproductiveEvents: List<ReproductiveEvent> = reproductiveEventsRaw
                    .filter { it.doeId == goatId || it.buckId == goatId }
                    .sortedByDescending { it.date }

                val nextExpectedBirth = reproductiveEvents
                    .filter {
                        it.doeId == goatId &&
                            it.eventType == ReproductiveEventType.BREEDING &&
                            it.result == ReproductiveEventResult.PENDING
                    }
                    .mapNotNull { expectedBirthDateOrNull(it.eventType, it.date) }
                    .minOrNull()

                val healthRecords = healthRecordsRaw
                    .filter { it.goatId == goatId }
                    .sortedByDescending { it.date }

                val milkRecords = milkRecordsRaw
                    .filter { it.goatId == goatId }
                    .sortedBy { it.date }

                val veterinaryInsumos = insumos.filter { it.category == InsumoCategory.VETERINARY && it.active }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        goat = goat,
                        ageLabel = ageLabel(goat.birthDate, today),
                        motherName = mother?.name,
                        fatherName = father?.name ?: goat.externalFatherDescription,
                        lactationNumber = lactationNumber(goatId, reproductiveEvents),
                        weightRecords = weightRecords,
                        reproductiveEvents = reproductiveEvents,
                        nextExpectedBirth = nextExpectedBirth,
                        healthRecords = healthRecords,
                        milkRecords = milkRecords,
                        availableBucks = allGoats.filter { g -> g.sex == GoatSex.MALE && g.id != goatId },
                        availableDoes = allGoats.filter { g -> g.sex == GoatSex.FEMALE && g.id != goatId },
                        veterinaryInsumos = veterinaryInsumos
                    )
                }
            } catch (t: Throwable) {
                _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: "No se pudo cargar la ficha") }
            }
        }
    }

    fun onTabSelected(tab: GoatDetailTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onSaveWeight(date: kotlinx.datetime.LocalDate, weightKg: Double, bodyConditionScore: Int?, notes: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(savingAction = true) }
            try {
                weightRecordRepository.insert(
                    WeightRecord(
                        id = newId(),
                        goatId = goatId,
                        date = date,
                        weightKg = weightKg,
                        bodyConditionScore = bodyConditionScore,
                        notes = notes
                    )
                )
                load()
            } catch (t: Throwable) {
                _uiState.update { it.copy(savingAction = false, errorMessage = t.message ?: "No se pudo guardar la pesada") }
            }
        }
    }

    fun onSaveReproductiveEvent(form: ReproductiveEventFormResult) {
        viewModelScope.launch {
            _uiState.update { it.copy(savingAction = true) }
            try {
                // Composición racial de los cabritos: promedio de la de la madre y el padre
                // (herencia 50/50) — ver CLAUDE.md / business.averageBreedComposition. Si el
                // padre es un semental externo (no está en el hato), su aporte queda como
                // desconocido; el admin puede completarlo a mano desde la ficha del cabrito.
                val motherComposition = _uiState.value.goat?.breedComposition ?: emptyList()
                val fatherComposition = form.buckId
                    ?.let { id -> _uiState.value.availableBucks.firstOrNull { it.id == id } }
                    ?.breedComposition
                    ?: emptyList()
                val kidBreedComposition = averageBreedComposition(motherComposition, fatherComposition)

                // Si el evento es un parto exitoso, primero se crean las fichas de los
                // cabritos (para tener sus ids) y luego el evento queda vinculado a ellas
                // vía kid_ids, como pide docs/data_model.md.
                val kidIds = form.newKids.map { kid ->
                    val kidId = newId()
                    goatRepository.insert(
                        Goat(
                            id = kidId,
                            tagNumber = kid.tagNumber,
                            name = kid.name,
                            sex = kid.sex,
                            breedComposition = kidBreedComposition,
                            birthDate = form.date,
                            motherId = goatId,
                            fatherId = form.buckId,
                            currentStatus = GoatStatus.KID,
                            herdEntryDate = form.date,
                            origin = GoatOrigin.BORN_ON_FARM
                        )
                    )
                    kidId
                }

                reproductiveEventRepository.insert(
                    ReproductiveEvent(
                        id = newId(),
                        doeId = goatId,
                        eventType = form.eventType,
                        date = form.date,
                        buckId = form.buckId,
                        result = form.result,
                        kidsBornCount = form.kidsBornCount,
                        kidsAliveCount = form.kidsAliveCount,
                        kidIds = kidIds.ifEmpty { null },
                        notes = form.notes
                    )
                )

                // El estado resultante de la cabra es de definición manual (decisión explícita
                // del dueño: un aborto puede dejarla seca o en producción según la etapa de
                // gestación en que ocurrió, así que no se infiere automáticamente del evento).
                // Solo se actualiza si el admin lo eligió explícitamente en el formulario.
                form.resultingDoeStatus?.let { newStatus ->
                    _uiState.value.goat?.let { currentGoat ->
                        goatRepository.update(goatId, currentGoat.copy(currentStatus = newStatus))
                    }
                }

                load()
            } catch (t: Throwable) {
                _uiState.update { it.copy(savingAction = false, errorMessage = t.message ?: "No se pudo guardar el evento") }
            }
        }
    }

    fun onSaveHealthEvent(form: HealthEventFormResult) {
        viewModelScope.launch {
            _uiState.update { it.copy(savingAction = true) }
            try {
                healthRecordRepository.insert(
                    HealthRecord(
                        id = newId(),
                        goatId = goatId,
                        type = form.type,
                        date = form.date,
                        description = form.description,
                        insumoId = form.insumoId,
                        dosage = form.dosage,
                        quantityUsed = form.quantityUsed,
                        milkWithdrawalDays = form.milkWithdrawalDays,
                        cost = form.cost,
                        veterinarian = form.veterinarian,
                        nextSuggestedDate = form.nextSuggestedDate
                    )
                )
                load()
            } catch (t: Throwable) {
                _uiState.update { it.copy(savingAction = false, errorMessage = t.message ?: "No se pudo guardar el evento") }
            }
        }
    }
}
