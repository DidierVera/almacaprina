package com.didiprogrammer.almacaprina.ui.theme

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.figtree_bold
import almacaprina.shared.generated.resources.figtree_extrabold
import almacaprina.shared.generated.resources.figtree_medium
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font

/**
 * Figtree es la única familia tipográfica del módulo — cargada como recurso compartido
 * para que Android e iOS se vean idénticos (nada de fallback a la fuente del sistema).
 */
@Composable
fun figtreeFontFamily(): FontFamily = FontFamily(
    Font(Res.font.figtree_medium, weight = FontWeight.Medium),
    Font(Res.font.figtree_bold, weight = FontWeight.Bold),
    Font(Res.font.figtree_extrabold, weight = FontWeight.ExtraBold)
)

/**
 * Escala tipográfica única del módulo. Los 11 tokens marcados "(spec)" tienen tamaño/peso
 * exactos del sistema de diseño; el resto de los slots de Typography (displayLarge,
 * displaySmall, headlineLarge, bodyLarge) se completan en la misma familia y la misma
 * lógica de peso/tamaño decreciente para que ningún texto caiga en un estilo por defecto
 * de Material (que no sería Figtree).
 */
@Composable
fun fincaTypography(): Typography {
    val figtree = figtreeFontFamily()
    fun style(weight: FontWeight, size: androidx.compose.ui.unit.TextUnit, tracking: androidx.compose.ui.unit.TextUnit = 0.sp) =
        TextStyle(fontFamily = figtree, fontWeight = weight, fontSize = size, letterSpacing = tracking)

    return Typography(
        displayLarge = style(FontWeight.ExtraBold, 46.sp, (-1.4).sp),
        displayMedium = style(FontWeight.ExtraBold, 40.sp, (-1.2).sp), // (spec) cifra grande de dashboard
        displaySmall = style(FontWeight.ExtraBold, 34.sp, (-0.8).sp),
        headlineLarge = style(FontWeight.ExtraBold, 34.sp),
        headlineMedium = style(FontWeight.ExtraBold, 30.sp, (-0.6).sp), // (spec) título de pantalla
        headlineSmall = style(FontWeight.ExtraBold, 24.sp), // (spec) cifra de tile
        titleLarge = style(FontWeight.ExtraBold, 17.sp), // (spec) valor destacado
        titleMedium = style(FontWeight.Bold, 16.sp), // (spec) valor de fila
        titleSmall = style(FontWeight.Bold, 14.sp), // (spec) título de fila
        bodyLarge = style(FontWeight.Medium, 16.sp),
        bodyMedium = style(FontWeight.Medium, 14.sp), // (spec) cuerpo y labels
        bodySmall = style(FontWeight.Medium, 13.sp), // (spec) meta y detalle
        labelLarge = style(FontWeight.Bold, 17.sp), // (spec) label de botón
        labelMedium = style(FontWeight.Bold, 13.sp), // (spec) acción en texto
        labelSmall = style(FontWeight.Bold, 12.sp, 1.2.sp) // (spec) encabezado de sección — aplicar .uppercase() al texto
    )
}
