package com.didiprogrammer.almacaprina.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/** Escala de espaciado única del módulo — usar siempre Arrangement.spacedBy(Spacing.x), nunca padding por hijo. */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 10.dp
    val lg = 14.dp
    val xl = 18.dp
    val xxl = 24.dp
}

val LocalSpacing = staticCompositionLocalOf { Spacing }
