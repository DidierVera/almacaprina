package com.didiprogrammer.almacaprina.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** Radios del sistema — nada con esquinas rectas en superficies, nada por debajo de 16.dp. */
val ShapeMedium = RoundedCornerShape(16.dp) // filas de lista y alertas
val ShapeLarge = RoundedCornerShape(18.dp) // tiles y botones
val ShapeExtraLarge = RoundedCornerShape(20.dp) // Cards y contenedores grandes

/** Chips, pills, segmentados, barras de progreso e indicadores — para usar directo en Surface/Box. */
val ShapePill: Shape = CircleShape

/** Equivalente "pill" pero como CornerBasedShape — es lo único que acepta Shapes() de Material3. */
private val ShapePillCorner = RoundedCornerShape(percent = 50)

val FincaShapes = Shapes(
    extraSmall = ShapePillCorner,
    small = ShapePillCorner,
    medium = ShapeMedium,
    large = ShapeLarge,
    extraLarge = ShapeExtraLarge
)
