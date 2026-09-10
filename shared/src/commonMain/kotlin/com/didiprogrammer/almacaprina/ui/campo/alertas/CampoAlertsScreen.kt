package com.didiprogrammer.almacaprina.ui.campo.alertas

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.campo_alerts_empty_message
import almacaprina.shared.generated.resources.campo_alerts_eyebrow
import almacaprina.shared.generated.resources.campo_alerts_next_date_prefix
import almacaprina.shared.generated.resources.campo_alerts_title
import almacaprina.shared.generated.resources.campo_checklist_deleted_goat_fallback
import almacaprina.shared.generated.resources.weighing_list_tag_prefix
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.didiprogrammer.almacaprina.ui.components.AlmacaprinaCard
import com.didiprogrammer.almacaprina.ui.components.RefreshableContent
import com.didiprogrammer.almacaprina.ui.components.ScreenHeaderWithBack
import com.didiprogrammer.almacaprina.ui.components.label
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import com.didiprogrammer.almacaprina.ui.theme.Tinta
import com.didiprogrammer.almacaprina.ui.theme.TintaSuave
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Campo · alertas de salud pendientes — tocar una lleva a la ficha técnica (historial de
 * salud) de esa cabra. Ruta `campo/alertas`. */
@Composable
fun CampoAlertsScreen(
    onBack: () -> Unit,
    onGoatClick: (String) -> Unit,
    viewModel: CampoAlertsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        RefreshableContent(
            isLoading = uiState.isLoading,
            isRefreshing = false,
            onRefresh = {},
            modifier = Modifier.padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                item {
                    ScreenHeaderWithBack(
                        eyebrow = stringResource(Res.string.campo_alerts_eyebrow),
                        title = stringResource(Res.string.campo_alerts_title),
                        onBack = onBack
                    )
                }
                if (uiState.reminders.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(Spacing.xl)) {
                            Text(stringResource(Res.string.campo_alerts_empty_message), style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
                        }
                    }
                } else {
                    items(uiState.reminders, key = { it.healthRecord.id }) { reminder ->
                        val goat = reminder.goat
                        AlmacaprinaCard(onClick = goat?.let { { onGoatClick(it.id) } }) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(goat?.name ?: stringResource(Res.string.campo_checklist_deleted_goat_fallback), style = MaterialTheme.typography.titleSmall, color = Tinta)
                                if (goat != null) {
                                    Text(stringResource(Res.string.weighing_list_tag_prefix, goat.tagNumber), style = MaterialTheme.typography.bodySmall, color = TintaSuave)
                                }
                            }
                            Text(reminder.healthRecord.type.label(), style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
                            reminder.healthRecord.nextSuggestedDate?.let {
                                Text(
                                    stringResource(Res.string.campo_alerts_next_date_prefix, it.toString()),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TintaSuave
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
