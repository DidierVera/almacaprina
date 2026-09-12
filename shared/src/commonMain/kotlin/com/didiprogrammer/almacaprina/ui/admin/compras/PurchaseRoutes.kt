package com.didiprogrammer.almacaprina.ui.admin.compras

/** Rutas de la sub-navegación de Compras, colgadas del NavHost de AdminRootScreen. */
object PurchaseRoutes {
    const val HISTORY = "admin/mas/compras"
    const val NEW_PURCHASE = "admin/mas/compras/nueva"
    const val EDIT_PURCHASE_PATTERN = "admin/mas/compras/editar/{purchaseId}"

    fun editPurchase(purchaseId: String): String = "admin/mas/compras/editar/$purchaseId"
}
