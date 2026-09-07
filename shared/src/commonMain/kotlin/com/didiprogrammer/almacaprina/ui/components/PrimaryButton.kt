package com.didiprogrammer.almacaprina.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.didiprogrammer.almacaprina.ui.theme.BordeControl
import com.didiprogrammer.almacaprina.ui.theme.ShapeLarge
import com.didiprogrammer.almacaprina.ui.theme.SobreVerde
import com.didiprogrammer.almacaprina.ui.theme.TintaOff
import com.didiprogrammer.almacaprina.ui.theme.Verde
import com.didiprogrammer.almacaprina.ui.theme.VerdePress

/**
 * Único botón primario por pantalla (ver spec de diseño). Verde / pressed VerdePress /
 * disabled BordeControl con label TintaOff. Para acciones de pie, pasar
 * `modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)`.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val containerColor = when {
        !enabled || loading -> BordeControl
        isPressed -> VerdePress
        else -> Verde
    }
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = ShapeLarge,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = SobreVerde,
            disabledContainerColor = BordeControl,
            disabledContentColor = TintaOff
        ),
        modifier = Modifier.heightIn(min = 44.dp).then(modifier)
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = SobreVerde, strokeWidth = 2.dp)
        } else {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}
