package com.didiprogrammer.almacaprina.ui.campo.cabras

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.campo_goats_list_empty_message
import almacaprina.shared.generated.resources.campo_goats_list_eyebrow
import almacaprina.shared.generated.resources.campo_goats_list_search_placeholder
import almacaprina.shared.generated.resources.campo_goats_list_title
import almacaprina.shared.generated.resources.weighing_list_tag_prefix
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import com.didiprogrammer.almacaprina.ui.theme.Tinta
import com.didiprogrammer.almacaprina.ui.theme.TintaSuave
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Campo · lista del hato (de solo lectura) — entra a la ficha de una cabra para ver su
 * historial de salud (novedades). */
@Composable
fun CampoGoatListScreen(
    onBack: () -> Unit,
    onGoatClick: (String) -> Unit,
    viewModel: CampoGoatListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        RefreshableContent(
            isLoading = uiState.isLoading,
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.padding(padding)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.xxl),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ScreenHeaderWithBack(
                        eyebrow = stringResource(Res.string.campo_goats_list_eyebrow),
                        title = stringResource(Res.string.campo_goats_list_title),
                        onBack = onBack
                    )
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = viewModel::onQueryChanged,
                        placeholder = { Text(stringResource(Res.string.campo_goats_list_search_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                if (uiState.filteredGoats.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(modifier = Modifier.fillMaxWidth().padding(Spacing.xl)) {
                            Text(stringResource(Res.string.campo_goats_list_empty_message), style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
                        }
                    }
                } else {
                    items(uiState.filteredGoats, key = { it.id }) { goat ->
                        AlmacaprinaCard(onClick = { onGoatClick(goat.id) }) {
                            Text(goat.name, style = MaterialTheme.typography.titleSmall, color = Tinta)
                            Text(stringResource(Res.string.weighing_list_tag_prefix, goat.tagNumber), style = MaterialTheme.typography.bodySmall, color = TintaSuave)
                        }
                    }
                }
            }
        }
    }
}
