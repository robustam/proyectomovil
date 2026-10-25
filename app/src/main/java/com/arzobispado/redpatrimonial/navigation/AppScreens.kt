package com.arzobispado.redpatrimonial.navigation

/**
 * Todas las rutas de navegación de la app.
 * La 'sealed class' asegura que solo existan las rutas definidas aquí.
 */
sealed class AppScreens(val route: String) {
    // ---- Acceso ----
    object Login : AppScreens("login")
    object Register : AppScreens("registro")
    object RecuperarClave : AppScreens("recuperar_clave")

    // ---- Turista / común ----
    object Map : AppScreens("inicio")
    object Explorar : AppScreens("explorar")
    object Detail : AppScreens("detalle/{nodoId}") {
        fun createRoute(nodoId: Long) = "detalle/$nodoId"
    }
    object EventList : AppScreens("eventos")
    object Ticket : AppScreens("ticket")
    object RoutePlanner : AppScreens("rutas")
    object RutaDetalle : AppScreens("ruta/{rutaId}") {
        fun createRoute(rutaId: Long) = "ruta/$rutaId"
    }
    object CrearRuta : AppScreens("crear_ruta")
    object CommunityAlerts : AppScreens("avisos")
    object Perfil : AppScreens("perfil")

    // ---- Profesor / colegio ----
    object Salidas : AppScreens("salidas")
    object NuevaSalida : AppScreens("nueva_salida")

    // ---- Guía voluntario ----
    object PanelGuia : AppScreens("panel_guia")
    object Disponibilidad : AppScreens("disponibilidad")

    // ---- Encargado de templo ----
    object PanelEncargado : AppScreens("panel_encargado")
    object HorarioExcepcional : AppScreens("horario_excepcional")
    object PublicarEvento : AppScreens("publicar_evento")
    object PublicarAviso : AppScreens("publicar_aviso")
    object Solicitudes : AppScreens("solicitudes")
    object EditarFicha : AppScreens("editar_ficha")

    // ---- Administrador ----
    object PanelAdmin : AppScreens("panel_admin")
}
