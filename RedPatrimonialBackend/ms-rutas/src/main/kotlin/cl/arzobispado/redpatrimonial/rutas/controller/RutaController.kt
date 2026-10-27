package cl.arzobispado.redpatrimonial.rutas.controller

import cl.arzobispado.redpatrimonial.rutas.dto.*
import cl.arzobispado.redpatrimonial.rutas.service.GuiaService
import cl.arzobispado.redpatrimonial.rutas.service.RutaService
import cl.arzobispado.redpatrimonial.rutas.service.SalidaService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class RutaController(
    private val rutaService: RutaService,
    private val guiaService: GuiaService,
    private val salidaService: SalidaService
) {
    // ---------------- Rutas (CU06, CU09) ----------------

    @GetMapping("/rutas")
    fun rutas(@RequestParam(required = false) idCreador: Long?): List<RutaResponse> = rutaService.listar(idCreador)

    @GetMapping("/rutas/{id}")
    fun ruta(@PathVariable id: Long): RutaResponse = rutaService.buscar(id)

    @PostMapping("/rutas")
    @ResponseStatus(HttpStatus.CREATED)
    fun crearRuta(@Valid @RequestBody req: RutaRequest): RutaResponse = rutaService.crear(req)

    @PostMapping("/rutas/calcular")
    fun calcularRuta(@Valid @RequestBody req: RutaRequest): RutaResponse = rutaService.calcular(req)

    @DeleteMapping("/rutas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun eliminarRuta(@PathVariable id: Long) = rutaService.eliminar(id)

    // ---------------- Guías (CU16) ----------------

    @PostMapping("/guias")
    @ResponseStatus(HttpStatus.CREATED)
    fun crearGuia(@Valid @RequestBody req: GuiaRequest): GuiaResponse = guiaService.crear(req)

    @GetMapping("/guias")
    fun guias(@RequestParam(defaultValue = "true") soloActivos: Boolean): List<GuiaResponse> = guiaService.listar(soloActivos)

    @GetMapping("/guias/usuario/{idUsuario}")
    fun guiaPorUsuario(@PathVariable idUsuario: Long, @RequestParam(required = false) nombre: String?): GuiaResponse =
        guiaService.porUsuario(idUsuario, nombre)

    @GetMapping("/guias/disponibles")
    fun disponibles(@RequestParam fecha: String, @RequestParam hora: String): List<GuiaResponse> =
        guiaService.disponiblesPara(fecha, hora)

    @PutMapping("/guias/{id}/activo")
    fun cambiarActivo(@PathVariable id: Long, @RequestParam activo: Boolean): GuiaResponse = guiaService.cambiarActivo(id, activo)

    @GetMapping("/guias/{id}/disponibilidad")
    fun disponibilidad(@PathVariable id: Long): List<DisponibilidadResponse> = guiaService.disponibilidad(id)

    @PostMapping("/guias/{id}/disponibilidad")
    @ResponseStatus(HttpStatus.CREATED)
    fun agregarDisponibilidad(@PathVariable id: Long, @Valid @RequestBody req: DisponibilidadRequest): DisponibilidadResponse =
        guiaService.agregarDisponibilidad(id, req)

    @DeleteMapping("/disponibilidad/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun eliminarDisponibilidad(@PathVariable id: Long) = guiaService.eliminarDisponibilidad(id)

    // ---------------- Salidas pedagógicas (CU07, CU08, CU10, CU15, CU17, CU18) ----------------

    @GetMapping("/objetivos")
    fun objetivos(): List<ObjetivoDto> = salidaService.objetivos()

    @PostMapping("/salidas")
    @ResponseStatus(HttpStatus.CREATED)
    fun crearSalida(@Valid @RequestBody req: SalidaRequest): SalidaResponse = salidaService.crear(req)

    @GetMapping("/salidas")
    fun salidas(
        @RequestParam(required = false) idProfesor: Long?,
        @RequestParam(required = false) idGuia: Long?,
        @RequestParam(required = false) idNodo: Long?,
        @RequestParam(required = false) estado: String?
    ): List<SalidaResponse> = salidaService.listar(idProfesor, idGuia, idNodo, estado)

    @GetMapping("/salidas/{id}")
    fun salida(@PathVariable id: Long): SalidaResponse = salidaService.buscar(id)

    @PutMapping("/salidas/{id}/estado")
    fun cambiarEstadoSalida(@PathVariable id: Long, @Valid @RequestBody req: CambioEstadoRequest): SalidaResponse =
        salidaService.cambiarEstado(id, req)

    @PutMapping("/salidas/{id}/respuesta-guia")
    fun responderGuia(@PathVariable id: Long, @Valid @RequestBody req: CambioEstadoRequest): SalidaResponse =
        salidaService.responderGuia(id, req)

    @PutMapping("/salidas/{id}/guia/{idGuia}")
    fun asignarGuia(@PathVariable id: Long, @PathVariable idGuia: Long): SalidaResponse = salidaService.asignarGuia(id, idGuia)

    @PutMapping("/salidas/{id}/finalizar")
    fun finalizar(@PathVariable id: Long, @RequestBody req: CambioEstadoRequest): SalidaResponse = salidaService.finalizar(id, req)

    @PostMapping("/salidas/{id}/evaluacion")
    fun evaluar(@PathVariable id: Long, @Valid @RequestBody req: EvaluacionRequest): SalidaResponse = salidaService.evaluar(id, req)
}
