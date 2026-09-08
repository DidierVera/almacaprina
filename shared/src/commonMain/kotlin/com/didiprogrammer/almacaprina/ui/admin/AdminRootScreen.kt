package com.didiprogrammer.almacaprina.ui.admin

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import com.didiprogrammer.almacaprina.domain.model.GoatStatus
import com.didiprogrammer.almacaprina.ui.admin.ajustes.AdminSettingsScreen
import com.didiprogrammer.almacaprina.ui.admin.calendario.AdminCareTaskFormScreen
import com.didiprogrammer.almacaprina.ui.admin.calendario.AdminCareTaskListScreen
import com.didiprogrammer.almacaprina.ui.admin.calendario.CareTaskRoutes
import com.didiprogrammer.almacaprina.ui.admin.catalogo.AdminCatalogoScreen
import com.didiprogrammer.almacaprina.ui.admin.compras.AdminNewPurchaseScreen
import com.didiprogrammer.almacaprina.ui.admin.compras.AdminPurchaseHistoryScreen
import com.didiprogrammer.almacaprina.ui.admin.compras.PurchaseRoutes
import com.didiprogrammer.almacaprina.ui.admin.hato.AdminGoatDetailScreen
import com.didiprogrammer.almacaprina.ui.admin.hato.AdminGoatFormScreen
import com.didiprogrammer.almacaprina.ui.admin.hato.AdminHatoListScreen
import com.didiprogrammer.almacaprina.ui.admin.hato.HatoRoutes
import com.didiprogrammer.almacaprina.ui.admin.home.AdminHomeScreen
import com.didiprogrammer.almacaprina.ui.admin.mas.AdminMasMenuScreen
import com.didiprogrammer.almacaprina.ui.admin.produccion.AdminNewBatchScreen
import com.didiprogrammer.almacaprina.ui.admin.produccion.AdminProductionHistoryScreen
import com.didiprogrammer.almacaprina.ui.admin.produccion.ProductionRoutes

/**
 * Contenedor raíz del rol Compras/Admin: bottom navigation de 5 pestañas +
 * NavHost anidado propio. Se cuelga de la ruta "admin_home" del NavHost raíz
 * (ver ui/App.kt). El bottom bar se oculta en pantallas de "drill-in" (ficha
 * técnica, formularios) para que se sientan como una pila de navegación normal.
 */
private const val ROUTE_SETTINGS = "admin/ajustes"

@Composable
fun AdminRootScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = backStackEntry?.destination
            val isOnTabRoot = AdminTab.entries.any { tab -> currentDestination?.route == tab.matchRoute }
            if (isOnTabRoot) {
                NavigationBar {
                    AdminTab.entries.forEach { tab ->
                        val selected = currentDestination?.hierarchy?.any { it.route == tab.matchRoute } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(imageVector = tab.icon, contentDescription = tab.label()) },
                            label = { Text(tab.label()) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AdminTab.INICIO.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AdminTab.INICIO.route) {
                AdminHomeScreen(
                    onHerdStatusClick = { statusFilter ->
                        navController.navigate(HatoRoutes.listFilteredByStatus(statusFilter))
                    },
                    onSeeAllAlertsClick = { /* TODO: pantalla "Todas las alertas" */ },
                    onRegistrarCompraClick = { navController.navigate(PurchaseRoutes.NEW_PURCHASE) },
                    onNuevoLoteClick = { navController.navigate(ProductionRoutes.NEW_BATCH) },
                    onNuevaTareaClick = { navController.navigate(CareTaskRoutes.NEW_TASK) }
                )
            }

            // ---------- HATO (Sección 2) ----------
            composable(
                route = HatoRoutes.LIST_PATTERN,
                arguments = listOf(navArgument("status") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) { backStackEntry ->
                val statusArg = backStackEntry.arguments?.read { getStringOrNull("status") }
                val initialStatus = statusArg?.let { runCatching { GoatStatus.valueOf(it) }.getOrNull() }
                AdminHatoListScreen(
                    initialStatusFilter = initialStatus,
                    onGoatClick = { goat -> navController.navigate(HatoRoutes.detail(goat.id)) },
                    onNewGoatClick = { navController.navigate(HatoRoutes.NEW_GOAT) }
                )
            }
            composable(HatoRoutes.NEW_GOAT) {
                AdminGoatFormScreen(
                    goatId = null,
                    onSaved = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(
                route = HatoRoutes.DETAIL_PATTERN,
                arguments = listOf(navArgument("goatId") { type = NavType.StringType })
            ) { backStackEntry ->
                val goatId = backStackEntry.arguments?.read { getStringOrNull("goatId") }.orEmpty()
                AdminGoatDetailScreen(
                    goatId = goatId,
                    onBack = { navController.popBackStack() },
                    onNavigateToGoat = { id -> navController.navigate(HatoRoutes.detail(id)) },
                    onEditClick = { navController.navigate(HatoRoutes.editGoat(goatId)) }
                )
            }
            composable(
                route = HatoRoutes.EDIT_GOAT_PATTERN,
                arguments = listOf(navArgument("goatId") { type = NavType.StringType })
            ) { backStackEntry ->
                val goatId = backStackEntry.arguments?.read { getStringOrNull("goatId") }
                AdminGoatFormScreen(
                    goatId = goatId,
                    onSaved = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }

            // ---------- CATÁLOGO (Sección 3) ----------
            composable(AdminTab.CATALOGO.route) { AdminCatalogoScreen() }

            // ---------- PRODUCCIÓN (Sección 4) ----------
            composable(ProductionRoutes.HISTORY) {
                AdminProductionHistoryScreen(onNewBatchClick = { navController.navigate(ProductionRoutes.NEW_BATCH) })
            }
            composable(ProductionRoutes.NEW_BATCH) {
                AdminNewBatchScreen(
                    onSaved = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }

            // ---------- MÁS (menú) ----------
            composable(AdminTab.MAS.route) {
                AdminMasMenuScreen(
                    onComprasClick = { navController.navigate(PurchaseRoutes.HISTORY) },
                    onCalendarioClick = { navController.navigate(CareTaskRoutes.LIST) },
                    onAjustesClick = { navController.navigate(ROUTE_SETTINGS) }
                )
            }

            // ---------- MÁS · AJUSTES ----------
            composable(ROUTE_SETTINGS) {
                AdminSettingsScreen(onLoggedOut = onLogout)
            }

            // ---------- MÁS · COMPRAS (Sección 5) ----------
            composable(PurchaseRoutes.HISTORY) {
                AdminPurchaseHistoryScreen(onNewPurchaseClick = { navController.navigate(PurchaseRoutes.NEW_PURCHASE) })
            }
            composable(PurchaseRoutes.NEW_PURCHASE) {
                AdminNewPurchaseScreen(
                    onSaved = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }

            // ---------- MÁS · CALENDARIO DE TAREAS (Sección 6) ----------
            composable(CareTaskRoutes.LIST) {
                AdminCareTaskListScreen(
                    onTaskClick = { taskId -> navController.navigate(CareTaskRoutes.editTask(taskId)) },
                    onNewTaskClick = { navController.navigate(CareTaskRoutes.NEW_TASK) }
                )
            }
            composable(CareTaskRoutes.NEW_TASK) {
                AdminCareTaskFormScreen(
                    taskId = null,
                    onSaved = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(
                route = CareTaskRoutes.EDIT_TASK_PATTERN,
                arguments = listOf(navArgument("taskId") { type = NavType.StringType })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.read { getStringOrNull("taskId") }
                AdminCareTaskFormScreen(
                    taskId = taskId,
                    onSaved = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
        }
    }
}
