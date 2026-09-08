package com.didiprogrammer.almacaprina.ui.campo.ordeno

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.milking_entry_reason_dry
import almacaprina.shared.generated.resources.milking_entry_reason_other
import almacaprina.shared.generated.resources.milking_entry_reason_sick
import almacaprina.shared.generated.resources.milking_entry_reason_under_treatment
import almacaprina.shared.generated.resources.milking_session_evening_label
import almacaprina.shared.generated.resources.milking_session_morning_label
import almacaprina.shared.generated.resources.milking_summary_back_home_button
import almacaprina.shared.generated.resources.milking_summary_count_ratio
import almacaprina.shared.generated.resources.milking_summary_registered_goats_label
import almacaprina.shared.generated.resources.milking_summary_title_suffix
import almacaprina.shared.generated.resources.milking_summary_total_label
import almacaprina.shared.generated.resources.milking_summary_unmilked_label
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.didiprogrammer.almacaprina.business.formatQuantity
import com.didiprogrammer.almacaprina.domain.model.NoMilkingReason
import com.didiprogrammer.almacaprina.ui.components.AlmacaprinaCard
import com.didiprogrammer.almacaprina.ui.components.PrimaryButton
import com.didiprogrammer.almacaprina.ui.theme.SobreVerde
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import com.didiprogrammer.almacaprina.ui.theme.Tinta
import com.didiprogrammer.almacaprina.ui.theme.TintaSuave
import com.didiprogrammer.almacaprina.ui.theme.Verde
import org.jetbrains.compose.resources.stringResource

/** Campo · Ordeño — resumen de sesión. Ver mockup campo-Registrar ordeño-selection-sumary.png. */
@Composable
fun MilkingSummaryScreen(
    viewModel: MilkingSessionViewModel,
    onFinish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Mismas etiquetas (en minúscula) que el selector de motivo de MilkingEntryScreen —
    // se resuelven aquí porque joinToString() de más abajo no es un contexto @Composable.
    val reasonLabels = mapOf(
        NoMilkingReason.DRY to stringResource(Res.string.milking_entry_reason_dry).lowercase(),
        NoMilkingReason.SICK to stringResource(Res.string.milking_entry_reason_sick).lowercase(),
        NoMilkingReason.UNDER_TREATMENT to stringResource(Res.string.milking_entry_reason_under_treatment).lowercase(),
        NoMilkingReason.OTHER to stringResource(Res.string.milking_entry_reason_other).lowercase()
    )
    val reasonBreakdown = uiState.entries
        .mapNotNull { it.noMilkingReason }
        .groupingBy { it }
        .eachCount()
        .entries
        .joinToString(" · ") { (reason, count) -> "$count ${reasonLabels[reason]}" }

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(Spacing.xxl),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(shape = CircleShape, color = Verde, modifier = Modifier.size(64.dp)) {
                Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = SobreVerde, modifier = Modifier.size(32.dp))
                }
            }
            androidx.compose.foundation.layout.Spacer(Modifier.size(Spacing.xl))
            val sessionLabel = stringResource(if (uiState.isEveningSession) Res.string.milking_session_evening_label else Res.string.milking_session_morning_label)
            Text(stringResource(Res.string.milking_summary_title_suffix, sessionLabel), style = MaterialTheme.typography.headlineMedium, color = Tinta)

            androidx.compose.foundation.layout.Spacer(Modifier.size(Spacing.xxl))
            Text(stringResource(Res.string.milking_summary_total_label), style = MaterialTheme.typography.labelMedium, color = TintaSuave)
            Text("${formatQuantity(uiState.totalLiters)} L", style = MaterialTheme.typography.displayLarge, color = Tinta)

            androidx.compose.foundation.layout.Spacer(Modifier.size(Spacing.xl))
            AlmacaprinaCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(Res.string.milking_summary_registered_goats_label), style = MaterialTheme.typography.bodyMedium, color = Tinta)
                    Text(stringResource(Res.string.milking_summary_count_ratio, uiState.registeredCount, uiState.totalCount), style = MaterialTheme.typography.titleSmall, color = Tinta)
                }
            }
            androidx.compose.foundation.layout.Spacer(Modifier.size(Spacing.sm))
            if (uiState.unmilkedCount > 0) {
                AlmacaprinaCard(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(stringResource(Res.string.milking_summary_unmilked_label), style = MaterialTheme.typography.bodyMedium, color = Tinta)
                            Text(reasonBreakdown, style = MaterialTheme.typography.bodySmall, color = TintaSuave)
                        }
                        Text("${uiState.unmilkedCount}", style = MaterialTheme.typography.titleSmall, color = Tinta)
                    }
                }
            }

            androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
            PrimaryButton(
                text = stringResource(Res.string.milking_summary_back_home_button),
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
