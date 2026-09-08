package com.didiprogrammer.almacaprina.ui.admin.catalogo

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_catalog_active_label
import almacaprina.shared.generated.resources.admin_goat_form_name_label
import almacaprina.shared.generated.resources.admin_product_form_category_label
import almacaprina.shared.generated.resources.admin_product_form_edit_title
import almacaprina.shared.generated.resources.admin_product_form_new_title
import almacaprina.shared.generated.resources.admin_product_form_price_label
import almacaprina.shared.generated.resources.admin_product_form_sale_unit_label
import almacaprina.shared.generated.resources.common_cancel
import almacaprina.shared.generated.resources.common_save_button
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.didiprogrammer.almacaprina.domain.model.Product
import com.didiprogrammer.almacaprina.domain.model.ProductCategory
import com.didiprogrammer.almacaprina.domain.model.SaleUnit
import com.didiprogrammer.almacaprina.ui.components.label
import org.jetbrains.compose.resources.stringResource

/**
 * Catálogo > Productos > "+ Nuevo producto" / editar uno existente.
 * `existing` nulo = alta; no nulo = edición (prellena los campos y guarda con update).
 */
@Composable
fun ProductFormDialog(
    existing: Product?,
    onDismiss: () -> Unit,
    onSave: (name: String, category: ProductCategory, saleUnit: SaleUnit, price: Double, active: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: ProductCategory.DERIVED_DAIRY) }
    var saleUnit by remember { mutableStateOf(existing?.saleUnit ?: SaleUnit.KILOGRAM) }
    var priceText by remember { mutableStateOf(existing?.defaultUnitPrice?.toString() ?: "") }
    var active by remember { mutableStateOf(existing?.active ?: true) }
    val price = priceText.toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (existing == null) Res.string.admin_product_form_new_title else Res.string.admin_product_form_edit_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(Res.string.admin_goat_form_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text(stringResource(Res.string.admin_product_form_category_label))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ProductCategory.entries) { c ->
                        FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c.label()) })
                    }
                }
                Text(stringResource(Res.string.admin_product_form_sale_unit_label))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(SaleUnit.entries) { u ->
                        FilterChip(selected = saleUnit == u, onClick = { saleUnit = u }, label = { Text(u.label()) })
                    }
                }
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text(stringResource(Res.string.admin_product_form_price_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(Res.string.admin_catalog_active_label))
                    Switch(checked = active, onCheckedChange = { active = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && price != null && price >= 0,
                onClick = { price?.let { onSave(name.trim(), category, saleUnit, it, active) } }
            ) { Text(stringResource(Res.string.common_save_button)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_cancel)) } }
    )
}
