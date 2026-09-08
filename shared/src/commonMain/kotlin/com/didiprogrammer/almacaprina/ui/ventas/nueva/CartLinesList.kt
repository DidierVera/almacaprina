package com.didiprogrammer.almacaprina.ui.ventas.nueva

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.new_sale_cart_edit_content_description
import almacaprina.shared.generated.resources.new_sale_cart_remove_content_description
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.didiprogrammer.almacaprina.business.formatCurrency
import com.didiprogrammer.almacaprina.business.formatQuantity
import com.didiprogrammer.almacaprina.domain.model.Packaging
import com.didiprogrammer.almacaprina.ui.components.label
import com.didiprogrammer.almacaprina.ui.theme.Borde
import com.didiprogrammer.almacaprina.ui.theme.ShapeLarge
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import com.didiprogrammer.almacaprina.ui.theme.Superficie
import com.didiprogrammer.almacaprina.ui.theme.Tinta
import com.didiprogrammer.almacaprina.ui.theme.TintaSuave
import org.jetbrains.compose.resources.stringResource

/**
 * Líneas ya confirmadas del carrito — se reutiliza en el Paso 3 (mientras se sigue agregando
 * productos) y en el Paso 4 (revisión final antes de guardar). Cada línea se puede editar
 * (vuelve al Paso 2 con sus datos precargados) o quitar directamente.
 */
@Composable
fun CartLinesList(
    lines: List<CartLine>,
    packagingsById: Map<String, Packaging>,
    currency: String,
    onEdit: (Int) -> Unit,
    onRemove: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        lines.forEachIndexed { index, line ->
            Surface(modifier = Modifier.fillMaxWidth(), shape = ShapeLarge, color = Superficie, border = BorderStroke(1.dp, Borde)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(line.product.name, style = MaterialTheme.typography.titleSmall, color = Tinta)
                        Text(
                            "${formatQuantity(line.quantity ?: 0.0)} ${line.product.saleUnit.label().lowercase()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TintaSuave
                        )
                    }
                    Text(
                        formatCurrency(line.total(packagingsById), currency),
                        style = MaterialTheme.typography.titleSmall,
                        color = Tinta,
                        modifier = Modifier.padding(horizontal = Spacing.sm)
                    )
                    IconButton(onClick = { onEdit(index) }) {
                        Icon(Icons.Filled.Edit, contentDescription = stringResource(Res.string.new_sale_cart_edit_content_description))
                    }
                    IconButton(onClick = { onRemove(index) }) {
                        Icon(Icons.Outlined.Close, contentDescription = stringResource(Res.string.new_sale_cart_remove_content_description))
                    }
                }
            }
        }
    }
}
