package com.didiprogrammer.almacaprina.ui.auth

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.pin_setup_confirm_label
import almacaprina.shared.generated.resources.pin_setup_error_mismatch
import almacaprina.shared.generated.resources.pin_setup_pin_label
import almacaprina.shared.generated.resources.pin_setup_save_button
import almacaprina.shared.generated.resources.pin_setup_skip_button
import almacaprina.shared.generated.resources.pin_setup_subtitle
import almacaprina.shared.generated.resources.pin_setup_title
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.didiprogrammer.almacaprina.data.remote.AuthService
import com.didiprogrammer.almacaprina.security.PinManager
import com.didiprogrammer.almacaprina.ui.components.FormField
import com.didiprogrammer.almacaprina.ui.components.PrimaryButton
import com.didiprogrammer.almacaprina.ui.theme.AmbarFondo
import com.didiprogrammer.almacaprina.ui.theme.AmbarTexto
import com.didiprogrammer.almacaprina.ui.theme.ShapeMedium
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import com.didiprogrammer.almacaprina.ui.theme.Tinta
import com.didiprogrammer.almacaprina.ui.theme.TintaSuave
import org.jetbrains.compose.resources.stringResource

private const val PIN_LENGTH = 4

/**
 * Se ofrece justo después del primer login exitoso (ver CLAUDE.md § Autenticación).
 * El PIN es solo un candado local del dispositivo — opcional, se puede omitir.
 */
@Composable
fun PinSetupScreen(onContinue: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isValid = pin.length == PIN_LENGTH && confirmPin.length == PIN_LENGTH
    val mismatchError = stringResource(Res.string.pin_setup_error_mismatch)

    Column(
        modifier = Modifier.fillMaxSize().padding(Spacing.xxl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(stringResource(Res.string.pin_setup_title), style = MaterialTheme.typography.headlineSmall, color = Tinta)
        Spacer(Modifier.height(Spacing.xs))
        Text(
            stringResource(Res.string.pin_setup_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = TintaSuave
        )
        Spacer(Modifier.height(Spacing.xxl))

        FormField(
            label = stringResource(Res.string.pin_setup_pin_label),
            value = pin,
            onValueChange = {
                if (it.length <= PIN_LENGTH && it.all(Char::isDigit)) pin = it
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(Spacing.md))
        FormField(
            label = stringResource(Res.string.pin_setup_confirm_label),
            value = confirmPin,
            onValueChange = {
                if (it.length <= PIN_LENGTH && it.all(Char::isDigit)) confirmPin = it
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation()
        )

        errorMessage?.let { message ->
            Spacer(Modifier.height(Spacing.md))
            Text(
                text = message,
                color = AmbarTexto,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AmbarFondo, ShapeMedium)
                    .padding(Spacing.lg)
            )
        }

        Spacer(Modifier.height(Spacing.xxl))

        PrimaryButton(
            text = stringResource(Res.string.pin_setup_save_button),
            enabled = isValid,
            onClick = {
                if (pin != confirmPin) {
                    errorMessage = mismatchError
                } else {
                    AuthService.currentUserId()?.let { userId -> PinManager.setPin(pin, userId) }
                    onContinue()
                }
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
        )
        Spacer(Modifier.height(Spacing.md))
        TextButton(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.pin_setup_skip_button))
        }
    }
}
