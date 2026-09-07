package com.didiprogrammer.almacaprina.ui.components

import com.didiprogrammer.almacaprina.domain.model.InsumoCategory
import com.didiprogrammer.almacaprina.domain.model.ProductCategory
import com.didiprogrammer.almacaprina.domain.model.PurchaseCategory
import com.didiprogrammer.almacaprina.domain.model.SaleUnit
import com.didiprogrammer.almacaprina.domain.model.UnitOfMeasure

/** Etiquetas en español de los enums del catálogo — funciones puras, sin contexto @Composable. */

fun ProductCategory.label(): String = when (this) {
    ProductCategory.RAW_MILK -> "Leche cruda"
    ProductCategory.DERIVED_DAIRY -> "Derivado"
}

fun SaleUnit.label(): String = when (this) {
    SaleUnit.LITER -> "Litro"
    SaleUnit.KILOGRAM -> "Kilogramo"
    SaleUnit.UNIT -> "Unidad"
}

fun InsumoCategory.label(): String = when (this) {
    InsumoCategory.FEED -> "Alimento"
    InsumoCategory.VETERINARY -> "Veterinario"
    InsumoCategory.PROCESSING_INPUT -> "Insumo de proceso"
    InsumoCategory.TRANSPORT -> "Transporte"
    InsumoCategory.LABOR -> "Mano de obra"
    InsumoCategory.MAINTENANCE -> "Mantenimiento"
    InsumoCategory.OTHER -> "Otro"
}

fun UnitOfMeasure.label(): String = when (this) {
    UnitOfMeasure.KG -> "kg"
    UnitOfMeasure.G -> "g"
    UnitOfMeasure.LITER -> "L"
    UnitOfMeasure.ML -> "ml"
    UnitOfMeasure.UNIT -> "unidad"
}

/** PurchaseCategory comparte los mismos valores que InsumoCategory más "packaging" (Sección 5). */
fun PurchaseCategory.label(): String = when (this) {
    PurchaseCategory.FEED -> "Alimento"
    PurchaseCategory.VETERINARY -> "Veterinario"
    PurchaseCategory.PACKAGING -> "Envase"
    PurchaseCategory.PROCESSING_INPUT -> "Insumo de proceso"
    PurchaseCategory.TRANSPORT -> "Transporte"
    PurchaseCategory.LABOR -> "Mano de obra"
    PurchaseCategory.MAINTENANCE -> "Mantenimiento"
    PurchaseCategory.OTHER -> "Otro"
}

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
