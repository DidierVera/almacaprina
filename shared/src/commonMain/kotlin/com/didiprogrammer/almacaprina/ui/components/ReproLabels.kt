package com.didiprogrammer.almacaprina.ui.components

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.repro_event_result_failed
import almacaprina.shared.generated.resources.repro_event_result_pending
import almacaprina.shared.generated.resources.repro_event_result_successful
import almacaprina.shared.generated.resources.repro_event_type_abortion
import almacaprina.shared.generated.resources.repro_event_type_birth
import almacaprina.shared.generated.resources.repro_event_type_breeding
import almacaprina.shared.generated.resources.repro_event_type_heat_detected
import almacaprina.shared.generated.resources.repro_event_type_pregnancy_diagnosis
import androidx.compose.runtime.Composable
import com.didiprogrammer.almacaprina.domain.model.ReproductiveEventResult
import com.didiprogrammer.almacaprina.domain.model.ReproductiveEventType
import org.jetbrains.compose.resources.stringResource

/** Etiquetas en español de ReproductiveEventType/Result — compartidas entre el formulario de
 * registro/edición y la pestaña "Reproducción" de la ficha técnica. */
@Composable
fun ReproductiveEventType.label(): String = stringResource(
    when (this) {
        ReproductiveEventType.HEAT_DETECTED -> Res.string.repro_event_type_heat_detected
        ReproductiveEventType.BREEDING -> Res.string.repro_event_type_breeding
        ReproductiveEventType.PREGNANCY_DIAGNOSIS -> Res.string.repro_event_type_pregnancy_diagnosis
        ReproductiveEventType.BIRTH -> Res.string.repro_event_type_birth
        ReproductiveEventType.ABORTION -> Res.string.repro_event_type_abortion
    }
)

@Composable
fun ReproductiveEventResult.label(): String = stringResource(
    when (this) {
        ReproductiveEventResult.PENDING -> Res.string.repro_event_result_pending
        ReproductiveEventResult.SUCCESSFUL -> Res.string.repro_event_result_successful
        ReproductiveEventResult.FAILED -> Res.string.repro_event_result_failed
    }
)
