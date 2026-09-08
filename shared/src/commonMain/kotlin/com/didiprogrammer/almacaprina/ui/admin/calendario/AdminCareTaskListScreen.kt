package com.didiprogrammer.almacaprina.ui.admin.calendario

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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_care_task_active_label
import almacaprina.shared.generated.resources.admin_care_task_group_prefix
import almacaprina.shared.generated.resources.admin_care_task_inactive_label
import almacaprina.shared.generated.resources.admin_care_task_insumo_prefix
import almacaprina.shared.generated.resources.admin_care_task_list_empty_message
import almacaprina.shared.generated.resources.admin_care_task_list_title
import almacaprina.shared.generated.resources.admin_home_new_task_action
import com.didiprogrammer.almacaprina.ui.components.AlmacaprinaCard
import com.didiprogrammer.almacaprina.ui.components.RefreshableContent
import com.didiprogrammer.almacaprina.ui.components.StatusChip
import com.didiprogrammer.almacaprina.ui.components.label
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Sección 6, pantalla 6.1 — Lista de tareas del calendario de cuidado. */
@Composable
fun AdminCareTaskListScreen(
    onTaskClick: (String) -> Unit,
    onNewTaskClick: () -> Unit,
    viewModel: AdminCareTaskListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    com.didiprogrammer.almacaprina.ui.components.RefreshOnResume(viewModel::load)

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(Res.string.admin_care_task_list_title)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewTaskClick) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(Res.string.admin_home_new_task_action))
            }
        }
    ) { padding ->
        RefreshableContent(
            isLoading = uiState.isLoading,
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.padding(padding)
        ) {
            if (uiState.items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(Res.string.admin_care_task_list_empty_message), style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.items, key = { it.task.id }) { item ->
                        CareTaskRow(item, onClick = { onTaskClick(item.task.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CareTaskRow(item: CareTaskListItem, onClick: () -> Unit) {
    val task = item.task
    AlmacaprinaCard(onClick = onClick) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(task.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${task.taskType.label()} · ${task.frequency.label()}",
                    style = MaterialTheme.typography.bodySmall
                )
                task.animalGroup?.let {
                    Text(stringResource(Res.string.admin_care_task_group_prefix, it.label()), style = MaterialTheme.typography.bodySmall)
                }
                item.insumoName?.let {
                    Text(stringResource(Res.string.admin_care_task_insumo_prefix, it), style = MaterialTheme.typography.bodySmall)
                }
            }
            val colors = MaterialTheme.colorScheme
            StatusChip(
                label = stringResource(if (task.active) Res.string.admin_care_task_active_label else Res.string.admin_care_task_inactive_label),
                containerColor = if (task.active) colors.primaryContainer else colors.surfaceVariant,
                contentColor = if (task.active) colors.onPrimaryContainer else colors.onSurfaceVariant
            )
        }
    }
}
