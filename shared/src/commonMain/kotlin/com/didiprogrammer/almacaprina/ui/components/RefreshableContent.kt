package com.didiprogrammer.almacaprina.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Envoltorio único para pantallas con lista/scroll — dos estados distintos, ver
 * discusión de UX: mientras `isLoading` (carga inicial, sin datos aún en pantalla) tapa
 * todo con un spinner grande; una vez cargada, el contenido queda envuelto en un
 * `PullToRefreshBox` para el gesto de "pull to refresh" (`isRefreshing`), que deja la
 * lista visible en vez de ocultarla mientras refresca.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshableContent(
    isLoading: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                content()
            }
        }
    }
}
