package cl.arzobispado.redpatrimonial.nodos.config

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

/** Se lanza cuando un registro buscado por id no existe (HTTP 404). */
class NoEncontradoException(mensaje: String) : RuntimeException(mensaje)

/** Se lanza cuando se viola una regla de negocio (HTTP 400). */
class ReglaNegocioException(mensaje: String) : RuntimeException(mensaje)

/** Formato estándar de error que reciben los clientes (la app móvil). */
data class ErrorRespuesta(
    val estado: Int,
    val error: String,
    val mensaje: String,
    val fecha: String = LocalDateTime.now().toString()
)

/** Convierte las excepciones en respuestas JSON claras. */
@RestControllerAdvice
class ManejadorErrores {

    @ExceptionHandler(NoEncontradoException::class)
    fun noEncontrado(e: NoEncontradoException) =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorRespuesta(404, "No encontrado", e.message ?: ""))

    @ExceptionHandler(ReglaNegocioException::class)
    fun reglaNegocio(e: ReglaNegocioException) =
        ResponseEntity.badRequest()
            .body(ErrorRespuesta(400, "Solicitud inválida", e.message ?: ""))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validacion(e: MethodArgumentNotValidException): ResponseEntity<ErrorRespuesta> {
        val detalle = e.bindingResult.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.badRequest().body(ErrorRespuesta(400, "Datos inválidos", detalle))
    }
}
