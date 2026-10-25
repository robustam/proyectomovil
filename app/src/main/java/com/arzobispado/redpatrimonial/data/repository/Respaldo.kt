package com.arzobispado.redpatrimonial.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

/** Se lanza en las vistas previas de Android Studio, donde no hay servidor. */
class SinServidorException : RuntimeException("Vista previa sin servidor")

/** Error de negocio devuelto por el backend (por ejemplo "No hay cupos suficientes"). */
class ErrorApi(mensaje: String) : Exception(mensaje)

/**
 * Indica si la app está conectada a los microservicios o trabajando con datos de prueba.
 * Las pantallas lo usan para mostrar un aviso "Modo sin conexión".
 */
object EstadoConexion {
    private val _enLinea = MutableStateFlow(true)
    val enLinea: StateFlow<Boolean> = _enLinea.asStateFlow()

    fun marcar(enLinea: Boolean) {
        _enLinea.value = enLinea
    }
}

/**
 * Ejecuta la llamada al backend. Si el servidor NO responde (no está encendido o no hay red),
 * usa los datos de prueba locales para que la app siga funcionando en la demostración.
 * Si el servidor responde con un error de negocio (400/404), se informa el mensaje al usuario.
 */
suspend fun <T> conRespaldo(remoto: suspend () -> T, local: () -> T): T =
    try {
        remoto().also { EstadoConexion.marcar(true) }
    } catch (e: HttpException) {
        EstadoConexion.marcar(true)
        throw ErrorApi(mensajeDe(e))
    } catch (e: IOException) {
        EstadoConexion.marcar(false)
        local()
    } catch (e: SinServidorException) {
        local()
    }

private fun mensajeDe(e: HttpException): String = try {
    val cuerpo = e.response()?.errorBody()?.string().orEmpty()
    JSONObject(cuerpo).optString("mensaje").ifBlank { "Error ${e.code()}" }
} catch (ex: Exception) {
    "Error del servidor (${e.code()})"
}
