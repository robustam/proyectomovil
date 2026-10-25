package com.arzobispado.redpatrimonial.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.arzobispado.redpatrimonial.data.model.Roles
import com.arzobispado.redpatrimonial.ui.components.Paleta
import com.arzobispado.redpatrimonial.ui.screens.*
import com.arzobispado.redpatrimonial.viewmodel.*

/** Ítem de la barra de navegación inferior. */
data class ItemMenu(val ruta: String, val etiqueta: String, val icono: ImageVector)

/**
 * Menú inferior según el rol. Así cada actor del anexo (turista, colegio,
 * encargado de templo) y el guía ven primero lo que necesitan.
 */
fun menuPara(rol: String): List<ItemMenu> {
    val mapa = ItemMenu(AppScreens.Map.route, "Mapa", Icons.Default.Map)
    val avisos = ItemMenu(AppScreens.CommunityAlerts.route, "Avisos", Icons.Default.Notifications)
    val perfil = ItemMenu(AppScreens.Perfil.route, "Perfil", Icons.Default.Person)
    val rutas = ItemMenu(AppScreens.RoutePlanner.route, "Rutas", Icons.Default.Explore)
    return when (rol) {
        Roles.PROFESOR -> listOf(mapa, ItemMenu(AppScreens.Salidas.route, "Salidas", Icons.Default.School), rutas, avisos, perfil)
        Roles.GUIA -> listOf(ItemMenu(AppScreens.PanelGuia.route, "Mi panel", Icons.Default.Badge), mapa, avisos, perfil)
        Roles.ENCARGADO -> listOf(ItemMenu(AppScreens.PanelEncargado.route, "Mi templo", Icons.Default.Church), mapa, avisos, perfil)
        Roles.ADMIN -> listOf(ItemMenu(AppScreens.PanelAdmin.route, "Panel", Icons.Default.Dashboard), mapa, avisos, perfil)
        else -> listOf(mapa, ItemMenu(AppScreens.Explorar.route, "Buscar", Icons.Default.Search), rutas, avisos, perfil)
    }
}

/** Primera pantalla después de iniciar sesión, según el rol. */
fun inicioPara(rol: String): String = menuPara(rol).first().ruta

/** Navega al inicio del rol y borra el historial (después de login, registro o logout). */
fun NavHostController.irAlInicio(rol: String) {
    navigate(inicioPara(rol)) {
        popUpTo(graph.id) { inclusive = true }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // ViewModels compartidos por todas las pantallas (viven mientras la app está abierta)
    val authVM: AuthViewModel = viewModel()
    val nodoVM: NodoViewModel = viewModel()
    val eventoVM: EventoViewModel = viewModel()
    val rutaVM: RutaViewModel = viewModel()
    val salidaVM: SalidaViewModel = viewModel()
    val guiaVM: GuiaViewModel = viewModel()
    val avisoVM: AvisoViewModel = viewModel()

    val usuario by authVM.usuario.collectAsState()
    val menu = menuPara(usuario?.rol ?: Roles.TURISTA)
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    val mostrarMenu = menu.any { it.ruta == rutaActual }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (mostrarMenu) {
                NavigationBar(containerColor = Color.White) {
                    menu.forEach { item ->
                        NavigationBarItem(
                            selected = rutaActual == item.ruta,
                            onClick = {
                                navController.navigate(item.ruta) {
                                    popUpTo(menu.first().ruta) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icono, contentDescription = item.etiqueta) },
                            label = { Text(item.etiqueta) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Paleta.Azul,
                                selectedTextColor = Paleta.Azul,
                                indicatorColor = Paleta.AzulSuave
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppScreens.Login.route,
            modifier = if (mostrarMenu) Modifier.padding(padding) else Modifier.navigationBarsPadding()
        ) {
            // ---------- Acceso ----------
            composable(AppScreens.Login.route) { LoginScreen(navController, authVM) }
            composable(AppScreens.Register.route) { RegisterScreen(navController, authVM) }
            composable(AppScreens.RecuperarClave.route) { RecuperarClaveScreen(navController, authVM) }

            // ---------- Turista / común ----------
            composable(AppScreens.Map.route) { MapScreen(navController, authVM, nodoVM, eventoVM, avisoVM) }
            composable(AppScreens.Explorar.route) { ExplorarScreen(navController, nodoVM) }
            composable(
                route = AppScreens.Detail.route,
                arguments = listOf(navArgument("nodoId") { type = NavType.LongType })
            ) { entrada ->
                val nodoId = entrada.arguments?.getLong("nodoId") ?: 0L
                DetailScreen(navController, nodoId, authVM, nodoVM, eventoVM, rutaVM)
            }
            composable(AppScreens.EventList.route) { EventListScreen(navController, authVM, eventoVM) }
            composable(AppScreens.Ticket.route) { TicketScreen(navController, eventoVM) }
            composable(AppScreens.RoutePlanner.route) { RoutePlannerScreen(navController, authVM, rutaVM) }
            composable(
                route = AppScreens.RutaDetalle.route,
                arguments = listOf(navArgument("rutaId") { type = NavType.LongType })
            ) { entrada ->
                val rutaId = entrada.arguments?.getLong("rutaId") ?: 0L
                RutaDetalleScreen(navController, rutaId, authVM, rutaVM, nodoVM)
            }
            composable(AppScreens.CrearRuta.route) { CrearRutaScreen(navController, authVM, rutaVM, nodoVM) }
            composable(AppScreens.CommunityAlerts.route) { CommunityAlertsScreen(navController, authVM, avisoVM, nodoVM) }
            composable(AppScreens.Perfil.route) { PerfilScreen(navController, authVM, eventoVM, rutaVM, nodoVM) }

            // ---------- Profesor ----------
            composable(AppScreens.Salidas.route) { SalidasScreen(navController, authVM, salidaVM) }
            composable(AppScreens.NuevaSalida.route) { NuevaSalidaScreen(navController, authVM, salidaVM, rutaVM) }

            // ---------- Guía ----------
            composable(AppScreens.PanelGuia.route) { PanelGuiaScreen(navController, authVM, guiaVM) }
            composable(AppScreens.Disponibilidad.route) { DisponibilidadScreen(navController, guiaVM) }

            // ---------- Encargado de templo ----------
            composable(AppScreens.PanelEncargado.route) { PanelEncargadoScreen(navController, authVM, nodoVM, salidaVM, avisoVM) }
            composable(AppScreens.HorarioExcepcional.route) { HorarioExcepcionalScreen(navController, authVM, nodoVM) }
            composable(AppScreens.PublicarEvento.route) { PublicarEventoScreen(navController, authVM, nodoVM, eventoVM) }
            composable(AppScreens.PublicarAviso.route) { PublicarAvisoScreen(navController, authVM, nodoVM, avisoVM) }
            composable(AppScreens.Solicitudes.route) { SolicitudesVisitaScreen(navController, authVM, salidaVM) }
            composable(AppScreens.EditarFicha.route) { EditarFichaScreen(navController, authVM, nodoVM) }

            // ---------- Administrador ----------
            composable(AppScreens.PanelAdmin.route) { PanelAdminScreen(navController, authVM, nodoVM, eventoVM, avisoVM) }
        }
    }
}
