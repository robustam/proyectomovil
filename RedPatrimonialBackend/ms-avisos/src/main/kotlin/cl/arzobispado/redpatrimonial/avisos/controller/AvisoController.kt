package cl.arzobispado.redpatrimonial.avisos.controller

import cl.arzobispado.redpatrimonial.avisos.dto.*
import cl.arzobispado.redpatrimonial.avisos.service.AvisoService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class AvisoController(private val service: AvisoService) {

    /** Avisos de la red en vivo: ?soloVigentes=&categoria=&idNodo= */
    @GetMapping("/avisos")
    fun listar(
        @RequestParam(defaultValue = "true") soloVigentes: Boolean,
        @RequestParam(required = false) categoria: String?,
        @RequestParam(required = false) idNodo: Long?
    ): List<AvisoResponse> = service.listar(soloVigentes, categoria, idNodo)

    @GetMapping("/avisos/{id}")
    fun buscar(@PathVariable id: Long): AvisoResponse = service.buscar(id)

    /** CU14 - Publicar aviso o alerta. */
    @PostMapping("/avisos")
    @ResponseStatus(HttpStatus.CREATED)
    fun publicar(@Valid @RequestBody req: AvisoRequest): AvisoResponse = service.publicar(req)

    @PutMapping("/avisos/{id}/retirar")
    fun retirar(@PathVariable id: Long): AvisoResponse = service.retirar(id)

    @PostMapping("/avisos/{id}/reportes")
    @ResponseStatus(HttpStatus.CREATED)
    fun reportar(@PathVariable id: Long, @Valid @RequestBody req: ReporteRequest): ReporteResponse = service.reportar(id, req)

    @GetMapping("/avisos/{id}/reportes")
    fun reportes(@PathVariable id: Long): List<ReporteResponse> = service.reportes(id)

    @GetMapping("/categorias-aviso")
    fun categorias(): List<CategoriaAvisoDto> = service.categorias()
}
