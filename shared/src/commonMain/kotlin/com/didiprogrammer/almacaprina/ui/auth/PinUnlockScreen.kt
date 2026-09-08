package com.didiprogrammer.almacaprina.ui.auth

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.pin_unlock_button
import almacaprina.shared.generated.resources.pin_unlock_error_incorrect
import almacaprina.shared.generated.resources.pin_unlock_error_profile_not_found
import almacaprina.shared.generated.resources.pin_unlock_forgot_button
import almacaprina.shared.generated.resources.pin_unlock_pin_label
import almacaprina.shared.generated.resources.pin_unlock_subtitle
import almacaprina.shared.generated.resources.pin_unlock_title
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.didiprogrammer.almacaprina.data.remote.AuthService
import com.didiprogrammer.almacaprina.domain.model.UserRole
import com.didiprogrammer.almacaprina.security.PinManager
import com.didiprogrammer.almacaprina.security.PinVerifyResult
import com.didiprogrammer.almacaprina.ui.components.FormField
import com.didiprogrammer.almacaprina.ui.components.PrimaryButton
import com.didiprogrammer.almacaprina.ui.theme.AmbarFondo
import com.didiprogrammer.almacaprina.ui.theme.AmbarTexto
import com.didiprogrammer.almacaprina.ui.theme.ShapeMedium
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import com.didiprogrammer.almacaprina.ui.theme.Tinta
import com.didiprogrammer.almacaprina.ui.theme.TintaSuave
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private const val PIN_LENGTH = 4

private sealed interface PinUnlockError {
    data class Incorrect(val remainingAttempts: Int) : PinUnlockError
    data object ProfileNotFound : PinUnlockError
}

/**
 * El PIN solo desbloquea la UI sobre una sesión de Supabase ya vigente — ver
 * CLAUDE.md § Autenticación. Tras [onLockedOut] la sesión se cierra y se fuerza
 * login completo con contraseña.
 */
@Composable
fun PinUnlockScreen(
    onUnlocked: (UserRole) -> Unit,
    onLockedOut: () -> Unit,
    onUsePasswordInstead: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    // Estado estructurado en vez de un String plano de error — stringResource() con
    // formatArgs (ej. "intentos restantes") es @Composable, así que el mensaje final
    // se resuelve más abajo, en el cuerpo del Composable, no dentro de submit().
    var error by remember { mutableStateOf<PinUnlockError?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun submit() {
        error = null
        when (PinManager.verify(pin)) {
            PinVerifyResult.CORRECT -> {
                isLoading = true
                scope.launch {
                    val profile = AuthService.fetchOwnProfile()
                    isLoading = false
                    if (profile != null) {
                        onUnlocked(profile.role)
                    } else {
                        error = PinUnlockError.ProfileNotFound
                    }
                }
            }
            PinVerifyResult.INCORRECT -> {
                pin = ""
                error = PinUnlockError.Incorrect(PinManager.remainingAttempts())
            }
            PinVerifyResult.LOCKED_OUT -> onLockedOut()
            PinVerifyResult.NO_PIN -> onUsePasswordInstead()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(Spacing.xxl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(stringResource(Res.string.pin_unlock_title), style = MaterialTheme.typography.headlineMedium, color = Tinta)
        Spacer(Modifier.height(Spacing.xs))
        Text(stringResource(Res.string.pin_unlock_subtitle), style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
        Spacer(Modifier.height(Spacing.xxl))

        FormField(
            label = stringResource(Res.string.pin_unlock_pin_label),
            value = pin,
            onValueChange = {
                if (it.length <= PIN_LENGTH && it.all(Char::isDigit)) {
                    pin = it
                    error = null
                    if (it.length == PIN_LENGTH) submit()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation()
        )

        error?.let { currentError ->
            val message = when (currentError) {
                is PinUnlockError.Incorrect -> stringResource(Res.string.pin_unlock_error_incorrect, currentError.remainingAttempts)
                PinUnlockError.ProfileNotFound -> stringResource(Res.string.pin_unlock_error_profile_not_found)
            }
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
            text = stringResource(Res.string.pin_unlock_button),
            enabled = pin.length == PIN_LENGTH && !isLoading,
            loading = isLoading,
            onClick = { submit() },
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
        )
        Spacer(Modifier.height(Spacing.md))
        TextButton(onClick = onUsePasswordInstead, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.pin_unlock_forgot_button))
        }
    }
}
