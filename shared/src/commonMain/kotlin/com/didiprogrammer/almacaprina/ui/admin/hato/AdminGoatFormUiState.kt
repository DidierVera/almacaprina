package com.didiprogrammer.almacaprina.ui.admin.hato

import com.didiprogrammer.almacaprina.business.breedCompositionTotal
import com.didiprogrammer.almacaprina.business.isLikelyAdult
import com.didiprogrammer.almacaprina.domain.model.Breed
import com.didiprogrammer.almacaprina.domain.model.BreedPercentage
import com.didiprogrammer.almacaprina.domain.model.Goat
import com.didiprogrammer.almacaprina.domain.model.GoatOrigin
import com.didiprogrammer.almacaprina.domain.model.GoatSex
import com.didiprogrammer.almacaprina.domain.model.GoatStatus
import com.didiprogrammer.almacaprina.util.newId
import kotlinx.datetime.LocalDate

/** Una fila editable del formulario de composición racial — `rowId` es solo para keys de Compose. */
data class BreedCompositionRow(
    val rowId: String = newId(),
    val breedName: String = "",
    val percentageText: String = ""
)

/** Formulario único para alta y edición de una cabra — `editingGoatId` nulo = alta. */
data class AdminGoatFormUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val editingGoatId: String? = null,
    val name: String = "",
    val tagNumber: String = "",
    val sex: GoatSex = GoatSex.FEMALE,
    val breedRows: List<BreedCompositionRow> = emptyList(),
    val birthDate: LocalDate? = null,
    val weaningDate: LocalDate? = null,
    val mother: Goat? = null,
    val father: Goat? = null,
    val externalFatherDescription: String = "",
    val origin: GoatOrigin = GoatOrigin.BORN_ON_FARM,
    val existingPhotoUrl: String? = null,
    val photoBytes: ByteArray? = null,
    val photoRemoved: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    /** Solo editable en modo edición — en alta se deriva o se pide como `initialStatus`. */
    val currentStatus: GoatStatus? = null,
    val initialStatus: GoatStatus? = null,
    val allGoats: List<Goat> = emptyList(),
    val today: LocalDate? = null,
    val breeds: List<Breed> = emptyList()
) {
    val isEditing: Boolean get() = editingGoatId != null

    /** Ver CLAUDE.md § altas manuales: solo se pide para compras de cabras adultas sin historial. */
    val requiresInitialStatus: Boolean
        get() = !isEditing && origin == GoatOrigin.PURCHASED && (today == null || isLikelyAdult(birthDate, today))

    val availableMothers: List<Goat> get() = allGoats.filter { it.sex == GoatSex.FEMALE && it.id != editingGoatId }
    val availableFathers: List<Goat> get() = allGoats.filter { it.sex == GoatSex.MALE && it.id != editingGoatId }

    /** Filas con nombre de raza no vacío, convertidas a la forma que se guarda en `Goat`. */
    val breedComposition: List<BreedPercentage>
        get() = breedRows
            .filter { it.breedName.isNotBlank() }
            .mapNotNull { row -> row.percentageText.toDoubleOrNull()?.let { BreedPercentage(row.breedName.trim(), it) } }

    /** Referencia informativa junto al editor — idealmente 100, pero no bloquea guardar. */
    val breedCompositionTotal: Double get() = breedCompositionTotal(breedComposition)

    val hasPhoto: Boolean get() = photoBytes != null || (existingPhotoUrl != null && !photoRemoved)

    val isValid: Boolean
        get() = name.isNotBlank() &&
            tagNumber.isNotBlank() &&
            birthDate != null &&
            (!requiresInitialStatus || initialStatus != null)
}
