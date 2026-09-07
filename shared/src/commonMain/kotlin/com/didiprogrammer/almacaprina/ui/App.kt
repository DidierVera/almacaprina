package com.didiprogrammer.almacaprina.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.didiprogrammer.almacaprina.data.remote.AuthService
import com.didiprogrammer.almacaprina.di.appModule
import com.didiprogrammer.almacaprina.domain.model.UserRole
import com.didiprogrammer.almacaprina.security.PinManager
import com.didiprogrammer.almacaprina.ui.admin.AdminRootScreen
import com.didiprogrammer.almacaprina.ui.auth.LoginScreen
import com.didiprogrammer.almacaprina.ui.auth.PinSetupScreen
import com.didiprogrammer.almacaprina.ui.auth.PinUnlockScreen
import com.didiprogrammer.almacaprina.ui.campo.CampoRootScreen
import com.didiprogrammer.almacaprina.ui.theme.FincaTheme
import com.didiprogrammer.almacaprina.ui.ventas.VentasRootScreen
import kotlinx.coroutines.launch
import org.koin.compose.KoinApplication

private const val ROUTE_SPLASH = "splash"
private const val ROUTE_LOGIN = "login"
private const val ROUTE_PIN_UNLOCK = "pin_unlock"
private const val PIN_SETUP_PATTERN = "pin_setup/{target}"
private const val ROUTE_CAMPO_HOME = "campo_home"
private const val ROUTE_VENTAS_HOME = "ventas_home"
private const val ROUTE_ADMIN_HOME = "admin_home"

private fun pinSetup(target: String): String = "pin_setup/$target"

private fun homeRouteFor(role: UserRole?): String = when (role) {
    UserRole.CAMPO -> ROUTE_CAMPO_HOME
    UserRole.VENTAS -> ROUTE_VENTAS_HOME
    UserRole.ADMIN -> ROUTE_ADMIN_HOME
    null -> ROUTE_LOGIN
}

@Composable
fun App() {
    // Registra el cliente de red (Ktor) que usa Coil3 para cargar photo_url
    // (fotos de cabras subidas a Supabase Storage) — una sola vez por app.
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
    }

    KoinApplication(application = { modules(appModule) }) {
        FincaTheme {
            val navController = rememberNavController()
            val scope = rememberCoroutineScope()

            NavHost(navController = navController, startDestination = ROUTE_SPLASH) {
                // Decide, al abrir la app, entre login completo, PIN o entrar directo —
                // según si hay una sesión de Supabase persistida y vigente (ver AuthService)
                // y si este dispositivo tiene un PIN configurado (ver CLAUDE.md § Autenticación).
                composable(ROUTE_SPLASH) {
                    LaunchedEffect(Unit) {
                        val destination = if (!AuthService.hasValidSession()) {
                            ROUTE_LOGIN
                        } else {
                            val userId = AuthService.currentUserId()
                            if (userId != null && PinManager.hasPin(userId)) {
                                ROUTE_PIN_UNLOCK
                            } else {
                                homeRouteFor(AuthService.fetchOwnProfile()?.role)
                            }
                        }
                        navController.navigate(destination) {
                            popUpTo(ROUTE_SPLASH) { inclusive = true }
                        }
                    }
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                composable(ROUTE_LOGIN) {
                    LoginScreen(
                        onLoginSuccess = { role ->
                            val destination = homeRouteFor(role)
                            val userId = AuthService.currentUserId()
                            if (userId != null && PinManager.hasPin(userId)) {
                                navController.navigate(destination) { popUpTo(ROUTE_LOGIN) { inclusive = true } }
                            } else {
                                navController.navigate(pinSetup(destination)) { popUpTo(ROUTE_LOGIN) { inclusive = true } }
                            }
                        }
                    )
                }

                composable(
                    route = PIN_SETUP_PATTERN,
                    arguments = listOf(navArgument("target") { type = NavType.StringType })
                ) { backStackEntry ->
                    val target = backStackEntry.arguments?.read { getStringOrNull("target") } ?: ROUTE_ADMIN_HOME
                    PinSetupScreen(
                        onContinue = {
                            navController.navigate(target) { popUpTo(pinSetup(target)) { inclusive = true } }
                        }
                    )
                }

                composable(ROUTE_PIN_UNLOCK) {
                    PinUnlockScreen(
                        onUnlocked = { role ->
                            navController.navigate(homeRouteFor(role)) { popUpTo(ROUTE_PIN_UNLOCK) { inclusive = true } }
                        },
                        onLockedOut = {
                            scope.launch { AuthService.signOut() }
                            navController.navigate(ROUTE_LOGIN) { popUpTo(ROUTE_PIN_UNLOCK) { inclusive = true } }
                        },
                        onUsePasswordInstead = {
                            PinManager.clearPin()
                            navController.navigate(ROUTE_LOGIN) { popUpTo(ROUTE_PIN_UNLOCK) { inclusive = true } }
                        }
                    )
                }

                // Campo y Ventas quedan fuera de este encargo (módulo de Compras/Admin).
                composable(ROUTE_CAMPO_HOME) {
                    CampoRootScreen(
                        onLogout = {
                            navController.navigate(ROUTE_LOGIN) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    )
                }
                composable(ROUTE_VENTAS_HOME) {
                    VentasRootScreen(
                        onLogout = {
                            navController.navigate(ROUTE_LOGIN) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    )
                }
                composable(ROUTE_ADMIN_HOME) {
                    AdminRootScreen(
                        onLogout = {
                            navController.navigate(ROUTE_LOGIN) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceholderHome(title: String, subtitle: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Bienvenido, $title", style = MaterialTheme.typography.headlineLarge)
            Text(subtitle, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
