package com.didiprogrammer.almacaprina.ui.auth

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

private const val PIN_LENGTH = 4

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
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun submit() {
        errorMessage = null
        when (PinManager.verify(pin)) {
            PinVerifyResult.CORRECT -> {
                isLoading = true
                scope.launch {
                    val profile = AuthService.fetchOwnProfile()
                    isLoading = false
                    if (profile != null) {
                        onUnlocked(profile.role)
                    } else {
                        errorMessage = "No se encontró un perfil configurado para este usuario."
                    }
                }
            }
            PinVerifyResult.INCORRECT -> {
                pin = ""
                errorMessage = "PIN incorrecto. Te quedan ${PinManager.remainingAttempts()} intento(s)."
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
        Text("Ingresa tu PIN", style = MaterialTheme.typography.headlineMedium, color = Tinta)
        Spacer(Modifier.height(Spacing.xs))
        Text("Almacaprina · desbloqueo rápido", style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
        Spacer(Modifier.height(Spacing.xxl))

        FormField(
            label = "PIN",
            value = pin,
            onValueChange = {
                if (it.length <= PIN_LENGTH && it.all(Char::isDigit)) {
                    pin = it
                    errorMessage = null
                    if (it.length == PIN_LENGTH) submit()
                }
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
            text = "Desbloquear",
            enabled = pin.length == PIN_LENGTH && !isLoading,
            loading = isLoading,
            onClick = { submit() },
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
        )
        Spacer(Modifier.height(Spacing.md))
        TextButton(onClick = onUsePasswordInstead, modifier = Modifier.fillMaxWidth()) {
            Text("Olvidé mi PIN — usar contraseña")
        }
    }
}
