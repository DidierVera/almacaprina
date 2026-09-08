package com.didiprogrammer.almacaprina.domain.model

import kotlinx.serialization.Serializable

/**
 * Maestro de razas — catálogo único de nombres de raza (ver CLAUDE.md / composición racial).
 * Reemplaza el texto libre que antes se escribía en cada fila de `breed_composition`, para
 * que dos fichas con la misma raza siempre coincidan exactamente y
 * `business.averageBreedComposition` pueda sumarlas por nombre sin errores de tipeo.
 * `prefix` es opcional — referencia informativa (ej. para nombrar/etiquetar crías).
 */
@Serializable
data class Breed(
    val id: String,
    val name: String,
    val prefix: String? = null
)
