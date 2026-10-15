package com.arzobispado.redpatrimonial.navigation

// 'sealed class' (clase sellada) asegura que solo las rutas aquí definidas
// existan en la navegación, evitando errores por escribir mal un texto.
sealed class AppScreens (val route: String){
    //identificadores de cada una de las pantallas
    object Login: AppScreens("login_screens")
    object Register: AppScreens("register_screens")
    object Map: AppScreens("map_screens")
    // Pantalla de Ficha Patrimonial (recibe dinámicamente el ID del templo)
    object Detail : AppScreens("detail_screen/{nodoId}") {
        fun createRoute(nodoId: Int): String = "detail_screen/$nodoId"
    }    //identificadores de eventos ,plantificador de salidas escolares
    // y de avisos comunitarios
    object EventList: AppScreens("event_list_screen")
    object RoutePlanner: AppScreens("route_planner_screen")
    object CommunityAlerts : AppScreens("community_alerts_screen")
}