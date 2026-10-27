package cl.arzobispado.redpatrimonial.eventos.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

/** Datos mínimos de un nodo obtenidos desde ms-nodos. */
data class NodoResumen(val idNodo: Long = 0, val nombreOficial: String = "", val latitud: Double? = null, val longitud: Double? = null)

/**
 * Comunicación entre microservicios: consulta ms-nodos para completar el nombre
 * y la ubicación del templo donde se publica el evento.
 */
@Component
class NodosClient(
    @Value("\${servicios.ms-nodos.url:http://localhost:8082}") private val urlNodos: String
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val cliente = RestClient.create()

    fun buscar(idNodo: Long): NodoResumen? = try {
        cliente.get().uri("$urlNodos/api/nodos/$idNodo").retrieve().body(NodoResumen::class.java)
    } catch (e: Exception) {
        log.warn("No se pudo consultar ms-nodos: {}", e.message)
        null
    }
}
