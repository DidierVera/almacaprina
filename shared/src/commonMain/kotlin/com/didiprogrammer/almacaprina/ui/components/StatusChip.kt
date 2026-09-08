package com.didiprogrammer.almacaprina.ui.components

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.goat_status_breeding_buck
import almacaprina.shared.generated.resources.goat_status_deceased
import almacaprina.shared.generated.resources.goat_status_dry
import almacaprina.shared.generated.resources.goat_status_in_production
import almacaprina.shared.generated.resources.goat_status_kid
import almacaprina.shared.generated.resources.goat_status_pregnant
import almacaprina.shared.generated.resources.goat_status_retired
import almacaprina.shared.generated.resources.goat_status_young_doe
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.didiprogrammer.almacaprina.domain.model.GoatStatus
import com.didiprogrammer.almacaprina.ui.theme.AmbarFondo
import com.didiprogrammer.almacaprina.ui.theme.AmbarTexto
import com.didiprogrammer.almacaprina.ui.theme.BordeControl
import com.didiprogrammer.almacaprina.ui.theme.Riel
import com.didiprogrammer.almacaprina.ui.theme.ShapePill
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import com.didiprogrammer.almacaprina.ui.theme.Superficie
import com.didiprogrammer.almacaprina.ui.theme.Tinta
import com.didiprogrammer.almacaprina.ui.theme.TintaOff
import org.jetbrains.compose.resources.stringResource

/**
 * Chip de estado genérico — mismo componente visual para cualquier "estado" de la app
 * (hato, tareas, compras, ventas). Forma ShapePill, borde BordeControl siempre; cada
 * pantalla solo decide el color de fondo/texto, tomado de los tokens del tema (nunca un
 * color suelto).
 *
 * DECISIÓN DE DISEÑO SIN CONFIRMAR: la paleta nueva es deliberadamente restringida ("un
 * solo acento cálido a la vez") y no define un esquema de color por estado para las 8
 * variantes de GoatStatus. Mapeé cada una de forma conservadora usando solo los tokens
 * neutros (Superficie/Riel) más Ambar* para "gestante" (el único estado con una noción de
 * fecha límite a vigilar, similar a una alerta). Si se quiere un color por estado más
 * expresivo, es una decisión de producto — no inventé tonos nuevos para resolverlo.
 */
@Composable
fun StatusChip(
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = ShapePill,
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, BordeControl)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)
        )
    }
}

/** Mapeo único de GoatStatus -> (color de fondo, color de texto, etiqueta en español). */
data class GoatStatusPresentation(val label: String, val container: Color, val content: Color)

/** Etiqueta en español de un GoatStatus — solo se puede llamar desde contexto @Composable. */
@Composable
fun GoatStatus.label(): String = stringResource(
    when (this) {
        GoatStatus.IN_PRODUCTION -> Res.string.goat_status_in_production
        GoatStatus.PREGNANT -> Res.string.goat_status_pregnant
        GoatStatus.DRY -> Res.string.goat_status_dry
        GoatStatus.YOUNG_DOE -> Res.string.goat_status_young_doe
        GoatStatus.KID -> Res.string.goat_status_kid
        GoatStatus.BREEDING_BUCK -> Res.string.goat_status_breeding_buck
        GoatStatus.RETIRED -> Res.string.goat_status_retired
        GoatStatus.DECEASED -> Res.string.goat_status_deceased
    }
)

@Composable
fun GoatStatus.presentation(): GoatStatusPresentation = when (this) {
    GoatStatus.IN_PRODUCTION -> GoatStatusPresentation(label(), Riel, Tinta)
    GoatStatus.YOUNG_DOE -> GoatStatusPresentation(label(), Riel, Tinta)
    GoatStatus.PREGNANT -> GoatStatusPresentation(label(), AmbarFondo, AmbarTexto)
    GoatStatus.DRY -> GoatStatusPresentation(label(), Superficie, Tinta)
    GoatStatus.BREEDING_BUCK -> GoatStatusPresentation(label(), Superficie, Tinta)
    GoatStatus.KID -> GoatStatusPresentation(label(), Superficie, Tinta)
    GoatStatus.RETIRED -> GoatStatusPresentation(label(), Superficie, TintaOff)
    GoatStatus.DECEASED -> GoatStatusPresentation(label(), Superficie, TintaOff)
}

@Composable
fun GoatStatusChip(status: GoatStatus, modifier: Modifier = Modifier) {
    val presentation = status.presentation()
    StatusChip(
        label = presentation.label,
        containerColor = presentation.container,
        contentColor = presentation.content,
        modifier = modifier
    )
}
