package com.didiprogrammer.almacaprina.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.didiprogrammer.almacaprina.ui.theme.Borde
import com.didiprogrammer.almacaprina.ui.theme.ShapeExtraLarge
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import com.didiprogrammer.almacaprina.ui.theme.Superficie

/**
 * Card base usada en todas las secciones (Inicio, Hato, Catálogo, Producción, Más).
 * Borde de 1.dp sin elevación (preferido del sistema), radio extraLarge (20.dp).
 */
@Composable
fun AlmacaprinaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(Spacing.xl),
    content: ColumnScopeContent
) {
    Surface(
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
        shape = ShapeExtraLarge,
        color = Superficie,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, Borde)
    ) {
        Column(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

private typealias ColumnScopeContent = @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
