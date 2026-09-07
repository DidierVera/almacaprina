package com.didiprogrammer.almacaprina.ui.admin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.PrecisionManufacturing
import androidx.compose.ui.graphics.vector.ImageVector
import com.didiprogrammer.almacaprina.ui.admin.hato.HatoRoutes
import com.didiprogrammer.almacaprina.ui.admin.produccion.ProductionRoutes

/**
 * Las 5 pestañas del bottom navigation del rol Compras/Admin.
 *
 * `route` es la URI concreta a la que se navega al tocar la pestaña. `matchRoute` es el
 * patrón exacto con el que Navigation-Compose registró el destino (lo que reporta
 * `NavDestination.route` en tiempo real) — para la mayoría de pestañas es igual a `route`,
 * pero Hato se registra con un patrón de query opcional (`admin/hato?status={status}`),
 * así que hace falta un valor de comparación distinto al de navegación.
 */
enum class AdminTab(val route: String, val label: String, val icon: ImageVector, val matchRoute: String = route) {
    INICIO("admin/inicio", "Inicio", Icons.Outlined.Home),
    HATO(HatoRoutes.LIST, "Hato", Icons.Outlined.Pets, matchRoute = HatoRoutes.LIST_PATTERN),
    CATALOGO("admin/catalogo", "Catálogo", Icons.AutoMirrored.Outlined.MenuBook),
    PRODUCCION(ProductionRoutes.HISTORY, "Producción", Icons.Outlined.PrecisionManufacturing),
    MAS("admin/mas", "Más", Icons.Outlined.MoreHoriz)
}
