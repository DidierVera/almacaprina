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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.didiprogrammer.almacaprina.data.remote.AuthService
import com.didiprogrammer.almacaprina.domain.model.UserRole
import com.didiprogrammer.almacaprina.ui.components.FormField
import com.didiprogrammer.almacaprina.ui.components.PrimaryButton
import com.didiprogrammer.almacaprina.ui.theme.AmbarFondo
import com.didiprogrammer.almacaprina.ui.theme.AmbarTexto
import com.didiprogrammer.almacaprina.ui.theme.ShapeMedium
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import com.didiprogrammer.almacaprina.ui.theme.Tinta
import com.didiprogrammer.almacaprina.ui.theme.TintaSuave
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (UserRole) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.xxl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text("Almacaprina", style = MaterialTheme.typography.headlineMedium, color = Tinta)
        Spacer(Modifier.height(Spacing.xs))
        Text(
            "Finca lechera · inicia sesión",
            style = MaterialTheme.typography.bodyMedium,
            color = TintaSuave
        )
        Spacer(Modifier.height(Spacing.xxl))

        FormField(
            label = "Correo",
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(Spacing.md))

        FormField(
            label = "Contraseña",
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(if (passwordVisible) "Ocultar" else "Mostrar")
                }
            }
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
            text = if (isLoading) "Ingresando…" else "Iniciar sesión",
            enabled = email.isNotBlank() && password.isNotBlank(),
            loading = isLoading,
            onClick = {
                errorMessage = null
                isLoading = true
                scope.launch {
                    try {
                        AuthService.signIn(email.trim(), password)
                        val profile = AuthService.fetchOwnProfile()
                        isLoading = false
                        if (profile != null) {
                            onLoginSuccess(profile.role)
                        } else {
                            errorMessage = "No se encontró un perfil configurado para este usuario."
                        }
                    } catch (e: Exception) {
                        isLoading = false
                        errorMessage = "Correo o contraseña incorrectos."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
        )

        Spacer(Modifier.height(Spacing.lg))
        Text(
            "Primera vez requiere conexión a internet.",
            style = MaterialTheme.typography.bodySmall,
            color = TintaSuave
        )
    }
}
