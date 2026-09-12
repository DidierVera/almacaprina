package com.didiprogrammer.almacaprina.ui.admin.hato

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_hato_empty_message
import almacaprina.shared.generated.resources.admin_hato_new_goat_content_description
import almacaprina.shared.generated.resources.admin_hato_search_placeholder
import almacaprina.shared.generated.resources.admin_hato_sort_label
import almacaprina.shared.generated.resources.admin_hato_status_filter_all
import almacaprina.shared.generated.resources.admin_hato_status_filter_title
import almacaprina.shared.generated.resources.admin_hato_tag_prefix
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.didiprogrammer.almacaprina.domain.model.Goat
import com.didiprogrammer.almacaprina.domain.model.GoatStatus
import com.didiprogrammer.almacaprina.ui.components.AlmacaprinaCard
import com.didiprogrammer.almacaprina.ui.components.ChipFilterRow
import com.didiprogrammer.almacaprina.ui.components.GoatAvatar
import com.didiprogrammer.almacaprina.ui.components.GoatStatusChip
import com.didiprogrammer.almacaprina.ui.components.RefreshOnResume
import com.didiprogrammer.almacaprina.ui.components.RefreshableContent
import com.didiprogrammer.almacaprina.ui.components.label
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Sección 2, pantalla 2.1 — Lista de Hato.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHatoListScreen(
    initialStatusFilter: GoatStatus?,
    onGoatClick: (Goat) -> Unit,
    onNewGoatClick: () -> Unit,
    viewModel: AdminHatoListViewModel = koinViewModel(parameters = { parametersOf(initialStatusFilter) })
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnResume(viewModel::load)

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewGoatClick) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(Res.string.admin_hato_new_goat_content_description))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(Res.string.admin_hato_search_placeholder)) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true
            )

            val statusFilterOptions = remember { listOf(null) + GoatStatus.entries }
            val allStatusLabel = stringResource(Res.string.admin_hato_status_filter_all)
            ChipFilterRow(
                options = statusFilterOptions,
                selected = uiState.selectedStatus,
                onSelect = viewModel::onStatusFilterSelected,
                label = { status -> if (status == null) allStatusLabel else status.label() },
                pickerTitle = stringResource(Res.string.admin_hato_status_filter_title),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Text(
                text = stringResource(Res.string.admin_hato_sort_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            val sortLabel = stringResource(Res.string.admin_hato_sort_label)
            ChipFilterRow(
                options = GoatSortOption.entries,
                selected = uiState.sortOption,
                onSelect = viewModel::onSortOptionSelected,
                label = { option -> option.label() },
                pickerTitle = sortLabel,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            RefreshableContent(
                isLoading = uiState.isLoading,
                isRefreshing = uiState.isRefreshing,
                onRefresh = viewModel::refresh
            ) {
                if (uiState.items.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(Res.string.admin_hato_empty_message), style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.items, key = { it.goat.id }) { item ->
                            GoatRow(item = item, onClick = { onGoatClick(item.goat) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoatRow(item: GoatListItem, onClick: () -> Unit) {
    AlmacaprinaCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GoatAvatar(name = item.goat.name, photoUrl = item.goat.photoUrl)
            Column(modifier = Modifier.weight(1f)) {
                Text(item.goat.name, style = MaterialTheme.typography.titleSmall)
                Text(stringResource(Res.string.admin_hato_tag_prefix, item.goat.tagNumber), style = MaterialTheme.typography.bodySmall)
                Text(
                    text = item.contextualInfo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            GoatStatusChip(status = item.goat.currentStatus)
        }
    }
}
