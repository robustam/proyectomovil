package cl.arzobispado.redpatrimonial.usuarios.controller

import cl.arzobispado.redpatrimonial.usuarios.dto.*
import cl.arzobispado.redpatrimonial.usuarios.service.UsuarioService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class UsuarioController(private val service: UsuarioService) {

    /** Registro desde la app (TURISTA, PROFESOR o GUIA). */
    @PostMapping("/auth/registro")
    @ResponseStatus(HttpStatus.CREATED)
    fun registrar(@Valid @RequestBody req: RegistroRequest): UsuarioResponse = service.registrar(req)

    /** Inicio de sesión: devuelve el usuario con su rol para que la app muestre su menú. */
    @PostMapping("/auth/login")
    fun login(@Valid @RequestBody req: LoginRequest): UsuarioResponse = service.login(req)

    @GetMapping("/usuarios")
    fun listar(): List<UsuarioResponse> = service.listar()

    @GetMapping("/usuarios/{id}")
    fun buscar(@PathVariable id: Long): UsuarioResponse = service.buscar(id)

    @PutMapping("/usuarios/{id}/estado")
    fun cambiarEstado(@PathVariable id: Long, @RequestParam estado: String): UsuarioResponse =
        service.cambiarEstado(id, estado)

    @GetMapping("/roles")
    fun roles(): List<RolResponse> = service.roles()

    /** CU20 - Gestionar permisos de usuarios por nodo. */
    @PostMapping("/permisos")
    @ResponseStatus(HttpStatus.CREATED)
    fun asignarPermiso(@Valid @RequestBody req: PermisoRequest): PermisoResponse = service.asignarPermiso(req)

    @GetMapping("/permisos/usuario/{idUsuario}")
    fun permisos(@PathVariable idUsuario: Long): List<PermisoResponse> = service.permisosDe(idUsuario)
}
