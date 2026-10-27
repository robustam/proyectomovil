package cl.arzobispado.redpatrimonial.eventos.controller

import cl.arzobispado.redpatrimonial.eventos.dto.*
import cl.arzobispado.redpatrimonial.eventos.service.EventoService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class EventoController(private val service: EventoService) {

    /** CU05 - Oferta cultural: ?idNodo=&categoria=&incluirPasados= */
    @GetMapping("/eventos")
    fun listar(
        @RequestParam(required = false) idNodo: Long?,
        @RequestParam(required = false) categoria: String?,
        @RequestParam(defaultValue = "false") incluirPasados: Boolean
    ): List<EventoResponse> = service.listar(idNodo, categoria, incluirPasados)

    @GetMapping("/eventos/{id}")
    fun buscar(@PathVariable id: Long): EventoResponse = service.buscar(id)

    /** CU12 - Publicar evento cultural o litúrgico. */
    @PostMapping("/eventos")
    @ResponseStatus(HttpStatus.CREATED)
    fun crear(@Valid @RequestBody req: EventoRequest): EventoResponse = service.crear(req)

    @PutMapping("/eventos/{id}")
    fun actualizar(@PathVariable id: Long, @Valid @RequestBody req: EventoRequest): EventoResponse =
        service.actualizar(id, req)

    @PutMapping("/eventos/{id}/cancelar")
    fun cancelar(@PathVariable id: Long): EventoResponse = service.cancelar(id)

    @GetMapping("/categorias-evento")
    fun categorias(): List<CategoriaDto> = service.categorias()

    /** Reservar cupo: devuelve el ticket con el código QR. */
    @PostMapping("/eventos/{id}/inscripciones")
    @ResponseStatus(HttpStatus.CREATED)
    fun inscribir(@PathVariable id: Long, @Valid @RequestBody req: InscripcionRequest): InscripcionResponse =
        service.inscribir(id, req)

    @GetMapping("/inscripciones/usuario/{idUsuario}")
    fun inscripciones(@PathVariable idUsuario: Long): List<InscripcionResponse> = service.inscripcionesDe(idUsuario)

    @PutMapping("/inscripciones/{id}/cancelar")
    fun cancelarInscripcion(@PathVariable id: Long): InscripcionResponse = service.cancelarInscripcion(id)

    /** Validar el QR en la entrada del recinto. */
    @PutMapping("/inscripciones/codigo/{codigo}/validar")
    fun validar(@PathVariable codigo: String): InscripcionResponse = service.validarCodigo(codigo)
}
