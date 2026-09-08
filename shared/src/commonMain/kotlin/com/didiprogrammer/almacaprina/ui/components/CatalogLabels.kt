package com.didiprogrammer.almacaprina.ui.components

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.insumo_category_feed
import almacaprina.shared.generated.resources.insumo_category_labor
import almacaprina.shared.generated.resources.insumo_category_maintenance
import almacaprina.shared.generated.resources.insumo_category_other
import almacaprina.shared.generated.resources.insumo_category_processing_input
import almacaprina.shared.generated.resources.insumo_category_transport
import almacaprina.shared.generated.resources.insumo_category_veterinary
import almacaprina.shared.generated.resources.product_category_derived_dairy
import almacaprina.shared.generated.resources.product_category_raw_milk
import almacaprina.shared.generated.resources.purchase_category_packaging
import almacaprina.shared.generated.resources.sale_unit_gram
import almacaprina.shared.generated.resources.sale_unit_kilogram
import almacaprina.shared.generated.resources.sale_unit_liter
import almacaprina.shared.generated.resources.sale_unit_unit
import almacaprina.shared.generated.resources.unit_of_measure_g
import almacaprina.shared.generated.resources.unit_of_measure_kg
import almacaprina.shared.generated.resources.unit_of_measure_liter
import almacaprina.shared.generated.resources.unit_of_measure_ml
import almacaprina.shared.generated.resources.unit_of_measure_unit
import androidx.compose.runtime.Composable
import com.didiprogrammer.almacaprina.domain.model.InsumoCategory
import com.didiprogrammer.almacaprina.domain.model.ProductCategory
import com.didiprogrammer.almacaprina.domain.model.PurchaseCategory
import com.didiprogrammer.almacaprina.domain.model.SaleUnit
import com.didiprogrammer.almacaprina.domain.model.UnitOfMeasure
import org.jetbrains.compose.resources.stringResource

/** Etiquetas en español de los enums del catálogo — solo se pueden llamar desde contexto @Composable. */

@Composable
fun ProductCategory.label(): String = stringResource(
    when (this) {
        ProductCategory.RAW_MILK -> Res.string.product_category_raw_milk
        ProductCategory.DERIVED_DAIRY -> Res.string.product_category_derived_dairy
    }
)

@Composable
fun SaleUnit.label(): String = stringResource(
    when (this) {
        SaleUnit.LITER -> Res.string.sale_unit_liter
        SaleUnit.KILOGRAM -> Res.string.sale_unit_kilogram
        SaleUnit.GRAM -> Res.string.sale_unit_gram
        SaleUnit.UNIT -> Res.string.sale_unit_unit
    }
)

@Composable
fun InsumoCategory.label(): String = stringResource(
    when (this) {
        InsumoCategory.FEED -> Res.string.insumo_category_feed
        InsumoCategory.VETERINARY -> Res.string.insumo_category_veterinary
        InsumoCategory.PROCESSING_INPUT -> Res.string.insumo_category_processing_input
        InsumoCategory.TRANSPORT -> Res.string.insumo_category_transport
        InsumoCategory.LABOR -> Res.string.insumo_category_labor
        InsumoCategory.MAINTENANCE -> Res.string.insumo_category_maintenance
        InsumoCategory.OTHER -> Res.string.insumo_category_other
    }
)

@Composable
fun UnitOfMeasure.label(): String = stringResource(
    when (this) {
        UnitOfMeasure.KG -> Res.string.unit_of_measure_kg
        UnitOfMeasure.G -> Res.string.unit_of_measure_g
        UnitOfMeasure.LITER -> Res.string.unit_of_measure_liter
        UnitOfMeasure.ML -> Res.string.unit_of_measure_ml
        UnitOfMeasure.UNIT -> Res.string.unit_of_measure_unit
    }
)

/** PurchaseCategory comparte los mismos valores que InsumoCategory más "packaging" (Sección 5) — reutiliza esas claves. */
@Composable
fun PurchaseCategory.label(): String = stringResource(
    when (this) {
        PurchaseCategory.FEED -> Res.string.insumo_category_feed
        PurchaseCategory.VETERINARY -> Res.string.insumo_category_veterinary
        PurchaseCategory.PACKAGING -> Res.string.purchase_category_packaging
        PurchaseCategory.PROCESSING_INPUT -> Res.string.insumo_category_processing_input
        PurchaseCategory.TRANSPORT -> Res.string.insumo_category_transport
        PurchaseCategory.LABOR -> Res.string.insumo_category_labor
        PurchaseCategory.MAINTENANCE -> Res.string.insumo_category_maintenance
        PurchaseCategory.OTHER -> Res.string.insumo_category_other
    }
)

/**
 * Mapea una categoría de compra a su equivalente de Insumo — permite filtrar el selector
 * de insumos en "Nueva compra" para que solo muestre los del rubro elegido. Null para
 * "packaging", que no tiene contraparte en InsumoCategory (usa el catálogo de Packaging).
 */
fun PurchaseCategory.toInsumoCategoryOrNull(): InsumoCategory? = when (this) {
    PurchaseCategory.FEED -> InsumoCategory.FEED
    PurchaseCategory.VETERINARY -> InsumoCategory.VETERINARY
    PurchaseCategory.PACKAGING -> null
    PurchaseCategory.PROCESSING_INPUT -> InsumoCategory.PROCESSING_INPUT
    PurchaseCategory.TRANSPORT -> InsumoCategory.TRANSPORT
    PurchaseCategory.LABOR -> InsumoCategory.LABOR
    PurchaseCategory.MAINTENANCE -> InsumoCategory.MAINTENANCE
    PurchaseCategory.OTHER -> InsumoCategory.OTHER
}
