package com.didiprogrammer.almacaprina.ui.auth

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

    Column(
        modifier = Modifier.fillMaxSize().padding(Spacing.xxl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text("Crea un PIN de acceso rápido", style = MaterialTheme.typography.headlineSmall, color = Tinta)
        Spacer(Modifier.height(Spacing.xs))
        Text(
            "La próxima vez podrás entrar con este PIN de 4 dígitos en vez de tu contraseña, " +
                "mientras tu sesión siga activa en este dispositivo. Es opcional.",
            style = MaterialTheme.typography.bodyMedium,
            color = TintaSuave
        )
        Spacer(Modifier.height(Spacing.xxl))

        FormField(
            label = "PIN (4 dígitos)",
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
            label = "Confirma el PIN",
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
            text = "Guardar PIN",
            enabled = isValid,
            onClick = {
                if (pin != confirmPin) {
                    errorMessage = "Los PIN no coinciden."
                } else {
                    AuthService.currentUserId()?.let { userId -> PinManager.setPin(pin, userId) }
                    onContinue()
                }
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
        )
        Spacer(Modifier.height(Spacing.md))
        TextButton(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text("Omitir por ahora")
        }
    }
}
