package com.didiprogrammer.almacaprina.ui.auth

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.app_icon
import almacaprina.shared.generated.resources.login_app_name
import almacaprina.shared.generated.resources.login_button_default
import almacaprina.shared.generated.resources.login_button_loading
import almacaprina.shared.generated.resources.login_email_label
import almacaprina.shared.generated.resources.login_error_invalid_credentials
import almacaprina.shared.generated.resources.login_error_profile_not_found
import almacaprina.shared.generated.resources.login_farm_subtitle_fallback
import almacaprina.shared.generated.resources.login_first_time_hint
import almacaprina.shared.generated.resources.login_password_hide
import almacaprina.shared.generated.resources.login_password_label
import almacaprina.shared.generated.resources.login_password_show
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.didiprogrammer.almacaprina.data.remote.AuthService
import com.didiprogrammer.almacaprina.domain.model.UserRole
import com.didiprogrammer.almacaprina.domain.repository.BusinessSettingsRepository
import com.didiprogrammer.almacaprina.ui.components.FormField
import com.didiprogrammer.almacaprina.ui.components.PrimaryButton
import com.didiprogrammer.almacaprina.ui.theme.AmbarFondo
import com.didiprogrammer.almacaprina.ui.theme.AmbarTexto
import com.didiprogrammer.almacaprina.ui.theme.ShapeMedium
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import com.didiprogrammer.almacaprina.ui.theme.Tinta
import com.didiprogrammer.almacaprina.ui.theme.TintaSuave
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun LoginScreen(
    onLoginSuccess: (UserRole) -> Unit,
    businessSettingsRepository: BusinessSettingsRepository = koinInject()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var farmName by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // stringResource() es @Composable — se resuelve aquí arriba para poder usarlo
    // dentro del onClick/coroutine del botón, que no es contexto composable.
    val profileNotFoundError = stringResource(Res.string.login_error_profile_not_found)
    val invalidCredentialsError = stringResource(Res.string.login_error_invalid_credentials)

    // Se trae sin sesión de Supabase (RLS con lectura pública, ver migración
    // 0008_business_settings_public_read.sql) — si falla (ej. sin internet), se
    // usa el subtítulo genérico de siempre en vez de romper el login.
    LaunchedEffect(Unit) {
        farmName = runCatching { businessSettingsRepository.getAll().firstOrNull()?.farmName?.takeIf { it.isNotBlank() } }
            .getOrNull()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.xxl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(Res.drawable.app_icon),
                contentDescription = null,
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp))
            )
            Spacer(Modifier.width(Spacing.md))
            Column {
                Text(farmName ?: stringResource(Res.string.login_farm_subtitle_fallback), style = MaterialTheme.typography.headlineMedium, color = Tinta)
                Text(
                    stringResource(Res.string.login_app_name),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TintaSuave
                )
            }
        }
        Spacer(Modifier.height(Spacing.xxl))

        FormField(
            label = stringResource(Res.string.login_email_label),
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(Spacing.md))

        FormField(
            label = stringResource(Res.string.login_password_label),
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(stringResource(if (passwordVisible) Res.string.login_password_hide else Res.string.login_password_show))
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
            text = stringResource(if (isLoading) Res.string.login_button_loading else Res.string.login_button_default),
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
                            errorMessage = profileNotFoundError
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        isLoading = false
                        errorMessage = invalidCredentialsError
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
        )

        Spacer(Modifier.height(Spacing.lg))
        Text(
            stringResource(Res.string.login_first_time_hint),
            style = MaterialTheme.typography.bodySmall,
            color = TintaSuave
        )
    }
}
