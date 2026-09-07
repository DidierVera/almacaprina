package com.didiprogrammer.almacaprina.ui.ventas

/** Rutas del módulo Ventas — ver tabla de convención de rutas en CLAUDE.md. */
object VentasRoutes {
    const val HOME = "ventas/inicio"

    /** Grafo anidado del wizard de nueva venta — no es una pantalla, agrupa los 4 pasos
     * para que compartan un mismo NewSaleViewModel via su propio back stack entry. */
    const val NEW_SALE_GRAPH = "ventas/nueva"

    const val NEW_SALE_CLIENTE = "ventas/nueva/cliente"
    const val NEW_SALE_PRODUCTO = "ventas/nueva/producto"
    const val NEW_SALE_CANTIDAD = "ventas/nueva/cantidad"
    const val NEW_SALE_CONFIRMAR = "ventas/nueva/confirmar"

    const val PENDING_LIST = "ventas/pendientes"
    const val PENDING_DETAIL_PATTERN = "ventas/pendientes/detalle/{customerId}"

    fun pendingDetail(customerId: String): String = "ventas/pendientes/detalle/$customerId"
}
