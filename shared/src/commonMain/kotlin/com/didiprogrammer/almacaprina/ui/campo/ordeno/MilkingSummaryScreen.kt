package com.didiprogrammer.almacaprina.ui.campo.ordeno

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
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

private fun NoMilkingReason.label(): String = when (this) {
    NoMilkingReason.DRY -> "seca"
    NoMilkingReason.SICK -> "enferma"
    NoMilkingReason.UNDER_TREATMENT -> "en tratamiento"
    NoMilkingReason.OTHER -> "otra razón"
}

/** Campo · Ordeño — resumen de sesión. Ver mockup campo-Registrar ordeño-selection-sumary.png. */
@Composable
fun MilkingSummaryScreen(
    viewModel: MilkingSessionViewModel,
    onFinish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val reasonBreakdown = uiState.entries
        .mapNotNull { it.noMilkingReason }
        .groupingBy { it }
        .eachCount()
        .entries
        .joinToString(" · ") { (reason, count) -> "$count ${reason.label()}" }

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
            Text("${uiState.sessionLabel} completa", style = MaterialTheme.typography.headlineMedium, color = Tinta)

            androidx.compose.foundation.layout.Spacer(Modifier.size(Spacing.xxl))
            Text("TOTAL DE LA SESIÓN", style = MaterialTheme.typography.labelMedium, color = TintaSuave)
            Text("${formatQuantity(uiState.totalLiters)} L", style = MaterialTheme.typography.displayLarge, color = Tinta)

            androidx.compose.foundation.layout.Spacer(Modifier.size(Spacing.xl))
            AlmacaprinaCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Cabras registradas", style = MaterialTheme.typography.bodyMedium, color = Tinta)
                    Text("${uiState.registeredCount} de ${uiState.totalCount}", style = MaterialTheme.typography.titleSmall, color = Tinta)
                }
            }
            androidx.compose.foundation.layout.Spacer(Modifier.size(Spacing.sm))
            if (uiState.unmilkedCount > 0) {
                AlmacaprinaCard(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Sin ordeñar", style = MaterialTheme.typography.bodyMedium, color = Tinta)
                            Text(reasonBreakdown, style = MaterialTheme.typography.bodySmall, color = TintaSuave)
                        }
                        Text("${uiState.unmilkedCount}", style = MaterialTheme.typography.titleSmall, color = Tinta)
                    }
                }
            }

            androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
            PrimaryButton(
                text = "Volver al inicio",
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
