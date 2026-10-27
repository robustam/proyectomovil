package cl.arzobispado.redpatrimonial.nodos.controller

import cl.arzobispado.redpatrimonial.nodos.dto.*
import cl.arzobispado.redpatrimonial.nodos.service.NodoService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class NodoController(private val service: NodoService) {

    /** CU01/CU02 - Lista de nodos con filtros: ?q=&tipo=&comuna=&soloAbiertos= */
    @GetMapping("/nodos")
    fun listar(
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) tipo: String?,
        @RequestParam(required = false) comuna: String?,
        @RequestParam(defaultValue = "false") soloAbiertos: Boolean
    ): List<NodoResponse> = service.listar(q, tipo, comuna, soloAbiertos)

    /** CU03 - Ficha patrimonial completa. */
    @GetMapping("/nodos/{id}")
    fun buscar(@PathVariable id: Long): NodoResponse = service.buscar(id)

    @PostMapping("/nodos")
    @ResponseStatus(HttpStatus.CREATED)
    fun crear(@Valid @RequestBody req: NodoRequest): NodoResponse = service.crear(req)

    /** CU13 - Modificar ficha. */
    @PutMapping("/nodos/{id}")
    fun actualizar(@PathVariable id: Long, @Valid @RequestBody req: NodoRequest): NodoResponse =
        service.actualizar(id, req)

    @DeleteMapping("/nodos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun eliminar(@PathVariable id: Long) = service.eliminar(id)

    /** CU04/CU11 - Cambiar estado de apertura (ABIERTO, CERRADO, EN_CELEBRACION). */
    @PutMapping("/nodos/{id}/estado")
    fun cambiarEstado(@PathVariable id: Long, @Valid @RequestBody req: EstadoRequest): NodoResponse =
        service.cambiarEstado(id, req.estado)

    @GetMapping("/nodos/{id}/horarios-excepcionales")
    fun horarios(@PathVariable id: Long): List<HorarioExcepcionalDto> = service.horariosExcepcionales(id)

    /** CU11 - Actualizar horarios excepcionales. */
    @PostMapping("/nodos/{id}/horarios-excepcionales")
    @ResponseStatus(HttpStatus.CREATED)
    fun agregarHorario(@PathVariable id: Long, @Valid @RequestBody req: HorarioExcepcionalRequest): HorarioExcepcionalDto =
        service.agregarHorarioExcepcional(id, req)

    @DeleteMapping("/horarios-excepcionales/{idHorario}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun eliminarHorario(@PathVariable idHorario: Long) = service.eliminarHorarioExcepcional(idHorario)

    /** CU13 - Galería multimedia. */
    @PostMapping("/nodos/{id}/recursos")
    @ResponseStatus(HttpStatus.CREATED)
    fun agregarRecurso(@PathVariable id: Long, @Valid @RequestBody req: RecursoRequest): RecursoDto =
        service.agregarRecurso(id, req)

    @DeleteMapping("/recursos/{idRecurso}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun eliminarRecurso(@PathVariable idRecurso: Long) = service.eliminarRecurso(idRecurso)

    @GetMapping("/catalogos/comunas")
    fun comunas(): List<CatalogoDto> = service.comunas()

    @GetMapping("/catalogos/tipos-nodo")
    fun tipos(): List<CatalogoDto> = service.tipos()

    @GetMapping("/catalogos/decanatos")
    fun decanatos(): List<CatalogoDto> = service.decanatos()

    /** CU21 - Indicadores de la red. */
    @GetMapping("/nodos/indicadores")
    fun indicadores(): IndicadoresDto = service.indicadores()
}
