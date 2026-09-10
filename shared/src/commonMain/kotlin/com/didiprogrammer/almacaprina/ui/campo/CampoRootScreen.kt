package com.didiprogrammer.almacaprina.ui.campo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import com.didiprogrammer.almacaprina.ui.campo.alertas.CampoAlertsScreen
import com.didiprogrammer.almacaprina.ui.campo.cabras.CampoGoatHealthDetailScreen
import com.didiprogrammer.almacaprina.ui.campo.cabras.CampoGoatListScreen
import com.didiprogrammer.almacaprina.ui.campo.checklist.CampoChecklistScreen
import com.didiprogrammer.almacaprina.ui.campo.novedad.CampoReportNovedadScreen
import com.didiprogrammer.almacaprina.ui.campo.ordeno.MilkingEntryScreen
import com.didiprogrammer.almacaprina.ui.campo.ordeno.MilkingSessionScreen
import com.didiprogrammer.almacaprina.ui.campo.ordeno.MilkingSessionViewModel
import com.didiprogrammer.almacaprina.ui.campo.ordeno.MilkingSummaryScreen
import com.didiprogrammer.almacaprina.ui.campo.pesada.CampoWeighingEntryScreen
import com.didiprogrammer.almacaprina.ui.campo.pesada.CampoWeighingListScreen
import org.koin.compose.viewmodel.koinViewModel

/** Rutas internas que agrupan la sesión de ordeño en un grafo anidado (mismo patrón que Ventas). */
private const val ORDENO_GRAPH = "campo/ordeno_graph"

/**
 * Contenedor raíz del rol Campo — NavHost propio, colgado de "campo_home" en el
 * NavHost raíz (ver ui/App.kt).
 */
@Composable
fun CampoRootScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = CampoRoutes.CHECKLIST) {
        composable(CampoRoutes.CHECKLIST) {
            CampoChecklistScreen(
                onOpenOrdeno = { navController.navigate(CampoRoutes.ORDENO_SESION) },
                onOpenPesada = { navController.navigate(CampoRoutes.PESADA_LISTA) },
                onReportNovedad = { navController.navigate(CampoRoutes.NOVEDAD) },
                onOpenCabras = { navController.navigate(CampoRoutes.CABRAS_LISTA) },
                onOpenAlertas = { navController.navigate(CampoRoutes.ALERTAS) },
                onLoggedOut = onLogout
            )
        }

        composable(CampoRoutes.ALERTAS) {
            CampoAlertsScreen(
                onBack = { navController.popBackStack() },
                onGoatClick = { goatId -> navController.navigate(CampoRoutes.cabraDetalle(goatId)) }
            )
        }

        composable(CampoRoutes.NOVEDAD) {
            CampoReportNovedadScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(CampoRoutes.CABRAS_LISTA) {
            CampoGoatListScreen(
                onBack = { navController.popBackStack() },
                onGoatClick = { goatId -> navController.navigate(CampoRoutes.cabraDetalle(goatId)) }
            )
        }
        composable(
            route = CampoRoutes.CABRAS_DETALLE_PATTERN,
            arguments = listOf(navArgument("goatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val goatId = backStackEntry.arguments?.read { getStringOrNull("goatId") }.orEmpty()
            CampoGoatHealthDetailScreen(
                goatId = goatId,
                onBack = { navController.popBackStack() }
            )
        }

        navigation(startDestination = CampoRoutes.ORDENO_SESION, route = ORDENO_GRAPH) {
            composable(CampoRoutes.ORDENO_SESION) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(ORDENO_GRAPH) }
                val viewModel: MilkingSessionViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
                MilkingSessionScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onGoatClick = { goatId -> navController.navigate(CampoRoutes.ordenoRegistro(goatId)) },
                    onFinishSession = { navController.navigate(CampoRoutes.ORDENO_RESUMEN) }
                )
            }
            composable(
                route = CampoRoutes.ORDENO_REGISTRO_PATTERN,
                arguments = listOf(navArgument("goatId") { type = NavType.StringType })
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(ORDENO_GRAPH) }
                val viewModel: MilkingSessionViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
                val goatId = backStackEntry.arguments?.read { getStringOrNull("goatId") }.orEmpty()
                MilkingEntryScreen(
                    goatId = goatId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable(CampoRoutes.ORDENO_RESUMEN) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(ORDENO_GRAPH) }
                val viewModel: MilkingSessionViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
                MilkingSummaryScreen(
                    viewModel = viewModel,
                    onFinish = {
                        navController.navigate(CampoRoutes.CHECKLIST) {
                            popUpTo(ORDENO_GRAPH) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(CampoRoutes.PESADA_LISTA) {
            CampoWeighingListScreen(
                onBack = { navController.popBackStack() },
                onGoatClick = { goatId -> navController.navigate(CampoRoutes.pesada(goatId)) }
            )
        }
        composable(
            route = CampoRoutes.PESADA_PATTERN,
            arguments = listOf(navArgument("goatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val goatId = backStackEntry.arguments?.read { getStringOrNull("goatId") }.orEmpty()
            CampoWeighingEntryScreen(
                goatId = goatId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
    }
}
