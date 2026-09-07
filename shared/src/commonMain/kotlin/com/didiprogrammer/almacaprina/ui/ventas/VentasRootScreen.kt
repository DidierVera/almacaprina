package com.didiprogrammer.almacaprina.ui.ventas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import com.didiprogrammer.almacaprina.ui.ventas.nueva.NewSaleCantidadScreen
import com.didiprogrammer.almacaprina.ui.ventas.nueva.NewSaleClienteScreen
import com.didiprogrammer.almacaprina.ui.ventas.nueva.NewSaleConfirmarScreen
import com.didiprogrammer.almacaprina.ui.ventas.nueva.NewSaleProductoScreen
import com.didiprogrammer.almacaprina.ui.ventas.nueva.NewSaleViewModel
import com.didiprogrammer.almacaprina.ui.ventas.home.VentasHomeScreen
import com.didiprogrammer.almacaprina.ui.ventas.pendientes.PendingSalesDetailScreen
import com.didiprogrammer.almacaprina.ui.ventas.pendientes.PendingSalesListScreen
import org.koin.compose.viewmodel.koinViewModel

/**
 * Contenedor raíz del rol Ventas — NavHost propio, colgado de "ventas_home" en el
 * NavHost raíz (ver ui/App.kt). Los 4 pasos de "Nueva venta" viven en un grafo anidado
 * (VentasRoutes.NEW_SALE_GRAPH) para compartir un mismo NewSaleViewModel entre pasos.
 *
 * Sin Scaffold propio a este nivel: cada pantalla ya trae su propio Scaffold (ver
 * VentasHomeScreen y el resto) que reserva los insets de sistema (status bar) —
 * envolver también el NavHost aquí duplicaría ese padding.
 */
@Composable
fun VentasRootScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = VentasRoutes.HOME) {
        composable(VentasRoutes.HOME) {
            VentasHomeScreen(
                onNuevaVentaClick = { navController.navigate(VentasRoutes.NEW_SALE_CLIENTE) },
                onPendientesClick = { navController.navigate(VentasRoutes.PENDING_LIST) },
                onLoggedOut = onLogout
            )
        }

        navigation(startDestination = VentasRoutes.NEW_SALE_CLIENTE, route = VentasRoutes.NEW_SALE_GRAPH) {
            composable(VentasRoutes.NEW_SALE_CLIENTE) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(VentasRoutes.NEW_SALE_GRAPH) }
                val viewModel: NewSaleViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
                NewSaleClienteScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onContinue = { navController.navigate(VentasRoutes.NEW_SALE_PRODUCTO) }
                )
            }
            composable(VentasRoutes.NEW_SALE_PRODUCTO) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(VentasRoutes.NEW_SALE_GRAPH) }
                val viewModel: NewSaleViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
                NewSaleProductoScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onContinue = { navController.navigate(VentasRoutes.NEW_SALE_CANTIDAD) }
                )
            }
            composable(VentasRoutes.NEW_SALE_CANTIDAD) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(VentasRoutes.NEW_SALE_GRAPH) }
                val viewModel: NewSaleViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
                NewSaleCantidadScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onContinue = { navController.navigate(VentasRoutes.NEW_SALE_CONFIRMAR) }
                )
            }
            composable(VentasRoutes.NEW_SALE_CONFIRMAR) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(VentasRoutes.NEW_SALE_GRAPH) }
                val viewModel: NewSaleViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
                NewSaleConfirmarScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onSaved = {
                        navController.navigate(VentasRoutes.HOME) {
                            popUpTo(VentasRoutes.NEW_SALE_GRAPH) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(VentasRoutes.PENDING_LIST) {
            PendingSalesListScreen(
                onBack = { navController.popBackStack() },
                onCustomerClick = { customerId -> navController.navigate(VentasRoutes.pendingDetail(customerId)) }
            )
        }
        composable(
            route = VentasRoutes.PENDING_DETAIL_PATTERN,
            arguments = listOf(navArgument("customerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.read { getStringOrNull("customerId") }.orEmpty()
            PendingSalesDetailScreen(
                customerId = customerId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
