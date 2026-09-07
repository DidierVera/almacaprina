package com.didiprogrammer.almacaprina.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.didiprogrammer.almacaprina.ui.theme.BordeControl
import com.didiprogrammer.almacaprina.ui.theme.Fondo
import com.didiprogrammer.almacaprina.ui.theme.ShapeLarge
import com.didiprogrammer.almacaprina.ui.theme.Superficie
import com.didiprogrammer.almacaprina.ui.theme.Tinta
import com.didiprogrammer.almacaprina.ui.theme.TintaOff

/** Botón secundario: borde BordeControl, fondo Superficie, pressed con fondo Fondo. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = ShapeLarge,
        interactionSource = interactionSource,
        border = BorderStroke(1.dp, BordeControl),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isPressed) Fondo else Superficie,
            contentColor = Tinta,
            disabledContentColor = TintaOff
        ),
        modifier = Modifier.heightIn(min = 44.dp).then(modifier)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
