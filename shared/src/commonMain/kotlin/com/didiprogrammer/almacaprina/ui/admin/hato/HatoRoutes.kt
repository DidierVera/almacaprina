package com.didiprogrammer.almacaprina.ui.admin.hato

/** Rutas de la sub-navegación de Hato, colgadas del NavHost de AdminRootScreen. */
object HatoRoutes {
    const val LIST = "admin/hato"
    const val LIST_PATTERN = "admin/hato?status={status}"
    const val NEW_GOAT = "admin/hato/nueva"
    const val DETAIL_PATTERN = "admin/hato/detalle/{goatId}"
    const val EDIT_GOAT_PATTERN = "admin/hato/editar/{goatId}"

    fun listFilteredByStatus(status: String): String = "admin/hato?status=$status"
    fun detail(goatId: String): String = "admin/hato/detalle/$goatId"
    fun editGoat(goatId: String): String = "admin/hato/editar/$goatId"
}
