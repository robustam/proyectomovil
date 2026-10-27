package cl.arzobispado.redpatrimonial.usuarios.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

/**
 * Cliente HTTP hacia el microservicio ms-rutas.
 * Si ms-rutas no está encendido, el registro igual funciona: el perfil de guía
 * se crea después automáticamente la primera vez que el guía entra a su panel.
 */
@Component
class IntegracionRutasClient(
    @Value("\${servicios.ms-rutas.url:http://localhost:8084}") private val urlRutas: String
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val cliente = RestClient.create()

    fun crearPerfilGuia(idUsuario: Long, nombre: String) {
        try {
            cliente.post()
                .uri("$urlRutas/api/guias")
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapOf("idUsuario" to idUsuario, "nombre" to nombre))
                .retrieve()
                .toBodilessEntity()
        } catch (e: Exception) {
            log.warn("No se pudo crear el perfil de guía en ms-rutas: {}", e.message)
        }
    }
}
