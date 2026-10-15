package com.arzobispado.redpatrimonial.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.arzobispado.redpatrimonial.ui.screens.LoginScreen

// Función Composable que administra los cambios de pantalla
@Composable
fun AppNavigation(){
// recuerda pantalla actual e historial de navegacion
    val  navController = rememberNavController()

    // NavHost es el contenedor donde se irán mostrando las pantallas.
    // 'startDestination' indica la pantalla con la que arranca la app.
    NavHost(
        navController= navController,
        startDestination = AppScreens.Login.route
    )
    // las rutas de navegacion la clase appscreen
    {
        composable (route = AppScreens.Login.route){ LoginScreen(navController = navController) }

        composable (route = AppScreens.Register.route){ }

        composable(route = AppScreens.Map.route){ }
// esta es la ruta de coneccion del appscreen deatil y los trae por id
        composable(route = AppScreens.Detail.route,
            arguments = listOf(navArgument("nodoId"){type= NavType.IntType}))
        {backStackEntry ->
            val nodoId = backStackEntry.arguments?.getInt("nodoId") ?:0

        }
        composable(route = AppScreens.EventList.route){}
        composable(route = AppScreens.RoutePlanner.route){}
        composable (route = AppScreens.CommunityAlerts.route){  }




    }
}