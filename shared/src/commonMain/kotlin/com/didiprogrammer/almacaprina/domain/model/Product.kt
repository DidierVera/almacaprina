package com.didiprogrammer.almacaprina.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ProductCategory {
    @SerialName("raw_milk") RAW_MILK,
    @SerialName("derived_dairy") DERIVED_DAIRY
}

@Serializable
enum class SaleUnit {
    @SerialName("liter") LITER,
    @SerialName("kilogram") KILOGRAM,
    @SerialName("gram") GRAM,
    @SerialName("unit") UNIT
}

@Serializable
data class Product(
    val id: String,
    val name: String,
    val category: ProductCategory,
    @SerialName("sale_unit") val saleUnit: SaleUnit,
    @SerialName("default_unit_price") val defaultUnitPrice: Double,
    val active: Boolean = true
)
