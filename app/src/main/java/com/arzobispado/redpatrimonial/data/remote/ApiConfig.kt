package com.arzobispado.redpatrimonial.data.remote

/**
 * Direcciones de los microservicios.
 *
 * - En el EMULADOR de Android, 10.0.2.2 apunta al "localhost" del computador.
 * - En un CELULAR FÍSICO, cambia HOST por la IP del computador en la red Wi-Fi
 *   (por ejemplo "192.168.1.15") y conecta ambos a la misma red.
 */
object ApiConfig {
    const val HOST = "10.0.2.2"

    const val URL_USUARIOS = "http://$HOST:8081/"
    const val URL_NODOS = "http://$HOST:8082/"
    const val URL_EVENTOS = "http://$HOST:8083/"
    const val URL_RUTAS = "http://$HOST:8084/"
    const val URL_AVISOS = "http://$HOST:8085/"

    /** Si el servidor no responde en este tiempo, la app usa los datos de prueba. */
    const val TIMEOUT_SEGUNDOS = 4L
}
