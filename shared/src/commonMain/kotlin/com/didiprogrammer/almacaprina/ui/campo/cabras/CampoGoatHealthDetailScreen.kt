package com.didiprogrammer.almacaprina.ui.campo.cabras

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_goat_detail_next_suggested_date
import almacaprina.shared.generated.resources.admin_goat_detail_no_health_records
import almacaprina.shared.generated.resources.campo_goat_health_eyebrow
import almacaprina.shared.generated.resources.weighing_list_tag_prefix
import androidx.compose.foundation.layout.Arrangement
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
import com.didiprogrammer.almacaprina.ui.theme.TintaSuave
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Campo · historial de salud de una cabra, de solo lectura. Ruta `campo/cabras/{goatId}`. */
@Composable
fun CampoGoatHealthDetailScreen(
    goatId: String,
    onBack: () -> Unit,
    viewModel: CampoGoatHealthDetailViewModel = koinViewModel(parameters = { parametersOf(goatId) })
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
                        eyebrow = stringResource(Res.string.campo_goat_health_eyebrow),
                        title = uiState.goat?.name ?: "",
                        onBack = onBack,
                        subtitle = uiState.goat?.tagNumber?.let { stringResource(Res.string.weighing_list_tag_prefix, it) }
                    )
                }
                if (uiState.healthRecords.isEmpty()) {
                    item { Text(stringResource(Res.string.admin_goat_detail_no_health_records), style = MaterialTheme.typography.bodyMedium, color = TintaSuave) }
                } else {
                    items(uiState.healthRecords, key = { it.id }) { record ->
                        AlmacaprinaCard {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(record.type.label(), style = MaterialTheme.typography.titleSmall)
                                Text(record.date.toString(), style = MaterialTheme.typography.bodySmall)
                            }
                            record.description?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                            record.nextSuggestedDate?.let {
                                Text(
                                    stringResource(Res.string.admin_goat_detail_next_suggested_date, it.toString()),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
