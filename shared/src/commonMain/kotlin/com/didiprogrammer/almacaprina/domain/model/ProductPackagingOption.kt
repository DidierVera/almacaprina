package com.didiprogrammer.almacaprina.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Relación muchos-a-muchos Product <-> Packaging — qué envases/empaques aplican a cada
 * producto (ver CLAUDE.md). Un mismo Packaging puede seguir asociado a varios productos.
 */
@Serializable
data class ProductPackagingOption(
    val id: String,
    @SerialName("product_id") val productId: String,
    @SerialName("packaging_id") val packagingId: String,
    @SerialName("is_default") val isDefault: Boolean = false
)
