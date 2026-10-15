package com.arzobispado.redpatrimonial.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.arzobispado.redpatrimonial.ui.screens.DetailScreen
import com.arzobispado.redpatrimonial.ui.screens.LoginScreen
import com.arzobispado.redpatrimonial.ui.screens.MapScreen
import com.arzobispado.redpatrimonial.ui.screens.RegisterScreen

// Función Composable que administra los cambios de pantalla
@Composable
fun AppNavigation() {
    // Recuerda pantalla actual e historial de navegación
    val navController = rememberNavController()

    // NavHost es el contenedor donde se irán mostrando las pantallas.
    NavHost(
        navController = navController,
        startDestination = AppScreens.Login.route
    ) {
        composable(route = AppScreens.Login.route) {
            LoginScreen(navController = navController)
        }

        composable(route = AppScreens.Register.route) {
            RegisterScreen(navController = navController)
        }

        composable(route = AppScreens.Map.route) {
            MapScreen(navController = navController)
        }

        composable(
            route = AppScreens.Detail.route,
            arguments = listOf(navArgument("nodoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val nodoId = backStackEntry.arguments?.getInt("nodoId") ?: 0
            DetailScreen(navController = navController, nodoId = nodoId)
        }

        // Rutas adicionales para futuras vistas
        composable(route = AppScreens.EventList.route) { }
        composable(route = AppScreens.RoutePlanner.route) { }
        composable(route = AppScreens.CommunityAlerts.route) { }
    }
}