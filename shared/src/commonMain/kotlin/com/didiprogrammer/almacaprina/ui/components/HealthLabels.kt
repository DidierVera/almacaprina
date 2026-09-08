package com.didiprogrammer.almacaprina.ui.components

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.health_record_type_deworming
import almacaprina.shared.generated.resources.health_record_type_diagnosis
import almacaprina.shared.generated.resources.health_record_type_routine_checkup
import almacaprina.shared.generated.resources.health_record_type_treatment
import almacaprina.shared.generated.resources.health_record_type_vaccine
import androidx.compose.runtime.Composable
import com.didiprogrammer.almacaprina.domain.model.HealthRecordType
import org.jetbrains.compose.resources.stringResource

/** Etiqueta en español de HealthRecordType — usada tanto por Admin (ficha técnica, registrar
 * evento de salud) como por Campo (checklist, recordatorios de salud individuales). */
@Composable
fun HealthRecordType.label(): String = stringResource(
    when (this) {
        HealthRecordType.VACCINE -> Res.string.health_record_type_vaccine
        HealthRecordType.DEWORMING -> Res.string.health_record_type_deworming
        HealthRecordType.TREATMENT -> Res.string.health_record_type_treatment
        HealthRecordType.ROUTINE_CHECKUP -> Res.string.health_record_type_routine_checkup
        HealthRecordType.DIAGNOSIS -> Res.string.health_record_type_diagnosis
    }
)
