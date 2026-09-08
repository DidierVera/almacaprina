package com.didiprogrammer.almacaprina.ui.campo.ordeno

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.milking_session_evening_label
import almacaprina.shared.generated.resources.milking_session_finish_button
import almacaprina.shared.generated.resources.milking_session_morning_label
import almacaprina.shared.generated.resources.milking_session_pending_label
import almacaprina.shared.generated.resources.milking_session_registered_count
import almacaprina.shared.generated.resources.milking_session_registered_label
import almacaprina.shared.generated.resources.milking_session_today_header
import almacaprina.shared.generated.resources.milking_session_unmilked_label
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.didiprogrammer.almacaprina.business.formatLongSpanishDate
import com.didiprogrammer.almacaprina.business.formatQuantity
import com.didiprogrammer.almacaprina.ui.components.AlmacaprinaCard
import com.didiprogrammer.almacaprina.ui.components.PrimaryButton
import com.didiprogrammer.almacaprina.ui.components.RefreshableContent
import com.didiprogrammer.almacaprina.ui.theme.Fondo
import com.didiprogrammer.almacaprina.ui.theme.Riel
import com.didiprogrammer.almacaprina.ui.theme.Terracota
import com.didiprogrammer.almacaprina.ui.theme.Tinta
import com.didiprogrammer.almacaprina.ui.theme.TintaSuave
import com.didiprogrammer.almacaprina.ui.theme.Verde
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import org.jetbrains.compose.resources.stringResource

/** Campo · Ordeño — lista de la sesión. Ver mockups campo-Registrar ordeño-selection-session*.png. */
@Composable
fun MilkingSessionScreen(
    viewModel: MilkingSessionViewModel,
    onBack: () -> Unit,
    onGoatClick: (String) -> Unit,
    onFinishSession: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState.allHandled) {
                Surface(color = Fondo) {
                    PrimaryButton(
                        text = stringResource(Res.string.milking_session_finish_button),
                        onClick = onFinishSession,
                        modifier = Modifier.fillMaxWidth().padding(24.dp)
                    )
                }
            }
        }
    ) { padding ->
        RefreshableContent(
            isLoading = uiState.isLoading,
            isRefreshing = false,
            onRefresh = {},
            modifier = Modifier.padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column {
                        Text(
                            stringResource(Res.string.milking_session_today_header, formatLongSpanishDate(today)).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = TintaSuave
                        )
                        Text(
                            stringResource(if (uiState.isEveningSession) Res.string.milking_session_evening_label else Res.string.milking_session_morning_label),
                            style = MaterialTheme.typography.displaySmall,
                            color = Tinta
                        )
                        Text(
                            stringResource(Res.string.milking_session_registered_count, uiState.registeredCount, uiState.totalCount),
                            style = MaterialTheme.typography.titleLarge,
                            color = Tinta
                        )
                        LinearProgressIndicator(
                            progress = { if (uiState.totalCount > 0) uiState.registeredCount.toFloat() / uiState.totalCount else 0f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).padding(top = 8.dp),
                            color = Terracota,
                            trackColor = Riel
                        )
                    }
                }

                items(uiState.entries, key = { it.goat.id }) { entry ->
                    AlmacaprinaCard(onClick = { onGoatClick(entry.goat.id) }) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(entry.goat.name, style = MaterialTheme.typography.titleSmall, color = Tinta)
                                Text(
                                    "${entry.goat.tagNumber} · ${stringResource(if (entry.registered) Res.string.milking_session_registered_label else Res.string.milking_session_pending_label)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TintaSuave
                                )
                            }
                            if (entry.sessionLiters != null) {
                                Text("${formatQuantity(entry.sessionLiters)} L", style = MaterialTheme.typography.titleMedium, color = Verde)
                            } else if (entry.noMilkingReason != null) {
                                Text(stringResource(Res.string.milking_session_unmilked_label), style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
                            }
                        }
                    }
                }
            }
        }
    }
}
