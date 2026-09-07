package com.didiprogrammer.almacaprina.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.LifecycleEventObserver

/**
 * Vuelve a ejecutar [onResume] cada vez que esta pantalla vuelve a quedar activa
 * (ON_RESUME de su NavBackStackEntry) — necesario porque Navigation-Compose reutiliza
 * la misma instancia de ViewModel al volver atrás con `popBackStack()`, o al cambiar
 * de pestaña del bottom nav con `restoreState = true` (ver AdminRootScreen). Un simple
 * `init { load() }` en el ViewModel no alcanza para reflejar cambios hechos en otra
 * pantalla (ej. crear una cabra y volver a la lista).
 */
@Composable
fun RefreshOnResume(onResume: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnResume by rememberUpdatedState(onResume)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                currentOnResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
