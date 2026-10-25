package com.arzobispado.redpatrimonial.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.arzobispado.redpatrimonial.data.mock.DatosPrueba
import com.arzobispado.redpatrimonial.data.model.Roles
import com.arzobispado.redpatrimonial.data.remote.*
import com.arzobispado.redpatrimonial.data.repository.*
import com.arzobispado.redpatrimonial.ui.theme.RedPatrimonialAppTheme
import com.arzobispado.redpatrimonial.viewmodel.*
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

/*
 * SOLO PARA LAS VISTAS PREVIAS (@Preview) DE ANDROID STUDIO.
 * La vista previa no tiene servidor: estas "APIs falsas" fallan de inmediato
 * y los repositorios usan los datos de prueba. La app real no usa este archivo.
 */

/** Crea una API que, en vez de llamar al servidor, lanza SinServidorException. */
private inline fun <reified T> apiSinServidor(): T {
    val manejador = InvocationHandler { proxy, metodo, args ->
        when (metodo.name) {
            "toString" -> "ApiSinServidor"
            "hashCode" -> 0
            "equals" -> proxy === args?.getOrNull(0)
            else -> throw SinServidorException()
        }
    }
    return Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java), manejador) as T
}

/** Todos los ViewModels de la app, conectados a los datos de prueba. */
class ViewModelsPrueba {
    val auth = AuthViewModel(AuthRepository(apiSinServidor<UsuarioApi>()))
    val nodo = NodoViewModel(NodoRepository(apiSinServidor<NodoApi>()))
    val evento = EventoViewModel(EventoRepository(apiSinServidor<EventoApi>()))
    val ruta = RutaViewModel(RutaRepository(apiSinServidor<RutaApi>()))
    val salida = SalidaViewModel(RutaRepository(apiSinServidor<RutaApi>()))
    val guia = GuiaViewModel(RutaRepository(apiSinServidor<RutaApi>()))
    val aviso = AvisoViewModel(AvisoRepository(apiSinServidor<AvisoApi>()))

    /** Usuario con el que se "inició sesión" en la vista previa (null = invitado). */
    val usuario get() = auth.usuario.value
}

/**
 * Prepara los ViewModels para una vista previa.
 * @param rol si se indica, inicia sesión con la cuenta demo de ese rol.
 * @param preparar carga adicional que normalmente haría la pantalla al abrirse.
 */
@Composable
fun rememberViewModelsPrueba(rol: String? = null, preparar: ViewModelsPrueba.() -> Unit = {}): ViewModelsPrueba =
    remember {
        ViewModelsPrueba().apply {
            if (rol != null) auth.login(correoDemo(rol), DatosPrueba.CLAVE_DEMO) {}
            preparar()
            EstadoConexion.marcar(true) // oculta el aviso "sin conexión" en la vista previa
        }
    }

private fun correoDemo(rol: String) = when (rol) {
    Roles.PROFESOR -> "profesor@demo.cl"
    Roles.GUIA -> "guia@demo.cl"
    Roles.ENCARGADO -> "encargado@demo.cl"
    Roles.ADMIN -> "admin@demo.cl"
    else -> "turista@demo.cl"
}

/** Aplica el tema de la app a la vista previa. */
@Composable
fun VistaPrevia(contenido: @Composable () -> Unit) {
    RedPatrimonialAppTheme { contenido() }
}
