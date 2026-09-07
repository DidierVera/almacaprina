package com.didiprogrammer.almacaprina.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Mapeo de la paleta a ColorScheme. Sin dark mode por ahora (deliberado — ver spec).
 * `error`/`errorContainer` no tienen tono dedicado en la paleta: se resuelven con el trío
 * Ambar* (ya es semánticamente "alerta" en este sistema — AlertRow lo usa igual).
 */
private val FincaColorScheme = lightColorScheme(
    primary = Verde,
    onPrimary = SobreVerde,
    primaryContainer = Verde,
    onPrimaryContainer = SobreVerde,
    secondary = Terracota,
    onSecondary = SobreVerde,
    secondaryContainer = Superficie,
    onSecondaryContainer = Terracota,
    tertiary = Terracota,
    onTertiary = SobreVerde,
    background = Fondo,
    onBackground = Tinta,
    surface = Superficie,
    onSurface = Tinta,
    surfaceVariant = Riel,
    onSurfaceVariant = TintaSuave,
    outline = Borde,
    outlineVariant = BordeControl,
    error = AmbarTexto,
    onError = AmbarTexto,
    errorContainer = AmbarFondo,
    onErrorContainer = AmbarTexto
)

/**
 * Tema visual único del módulo Compras/Admin. Ninguna pantalla debe envolver contenido con
 * MaterialTheme directamente ni declarar colores/tamaños/radios propios — todo sale de acá
 * (colorScheme, fincaTypography(), FincaShapes) y de Spacing vía LocalSpacing.
 */
@Composable
fun FincaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FincaColorScheme,
        typography = fincaTypography(),
        shapes = FincaShapes
    ) {
        CompositionLocalProvider(LocalSpacing provides Spacing) {
            content()
        }
    }
}
