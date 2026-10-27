package cl.arzobispado.redpatrimonial.usuarios

import cl.arzobispado.redpatrimonial.usuarios.config.ReglaNegocioException
import cl.arzobispado.redpatrimonial.usuarios.dto.LoginRequest
import cl.arzobispado.redpatrimonial.usuarios.dto.RegistroRequest
import cl.arzobispado.redpatrimonial.usuarios.model.Rol
import cl.arzobispado.redpatrimonial.usuarios.model.Usuario
import cl.arzobispado.redpatrimonial.usuarios.repository.PermisoNodoRepository
import cl.arzobispado.redpatrimonial.usuarios.repository.RolRepository
import cl.arzobispado.redpatrimonial.usuarios.repository.UsuarioRepository
import cl.arzobispado.redpatrimonial.usuarios.service.IntegracionRutasClient
import cl.arzobispado.redpatrimonial.usuarios.service.UsuarioService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

/** Pruebas unitarias de la lógica de registro e inicio de sesión. */
class UsuarioServiceTest {

    private val usuarioRepo = mock(UsuarioRepository::class.java)
    private val rolRepo = mock(RolRepository::class.java)
    private val permisoRepo = mock(PermisoNodoRepository::class.java)
    private val integracion = mock(IntegracionRutasClient::class.java)
    private lateinit var service: UsuarioService

    @BeforeEach
    fun preparar() {
        service = UsuarioService(usuarioRepo, rolRepo, permisoRepo, integracion)
        `when`(permisoRepo.findByUsuarioIdUsuario(1L)).thenReturn(emptyList())
    }

    @Test
    fun `no permite registrarse como ENCARGADO`() {
        val req = RegistroRequest("Ana", "ana@demo.cl", "123456", "ENCARGADO")
        assertThrows<ReglaNegocioException> { service.registrar(req) }
    }

    @Test
    fun `no permite correos repetidos`() {
        `when`(usuarioRepo.existsByEmailIgnoreCase("ana@demo.cl")).thenReturn(true)
        val req = RegistroRequest("Ana", "ana@demo.cl", "123456", "TURISTA")
        assertThrows<ReglaNegocioException> { service.registrar(req) }
    }

    @Test
    fun `registro guarda la clave cifrada`() {
        val rol = Rol(1, "TURISTA", "")
        `when`(usuarioRepo.existsByEmailIgnoreCase("ana@demo.cl")).thenReturn(false)
        `when`(rolRepo.findByNombreRol("TURISTA")).thenReturn(rol)
        `when`(usuarioRepo.save(any(Usuario::class.java))).thenAnswer { inv ->
            (inv.arguments[0] as Usuario).also { it.idUsuario = 1 }
        }

        val respuesta = service.registrar(RegistroRequest("Ana", "ana@demo.cl", "123456", "turista"))

        assertEquals("TURISTA", respuesta.rol)
        assertEquals("ana@demo.cl", respuesta.email)
    }

    @Test
    fun `login rechaza clave incorrecta`() {
        val hash = BCryptPasswordEncoder().encode("correcta")
        val usuario = Usuario(1, Rol(1, "TURISTA", ""), "Ana", "ana@demo.cl", hash)
        `when`(usuarioRepo.findByEmailIgnoreCase("ana@demo.cl")).thenReturn(usuario)

        assertThrows<ReglaNegocioException> { service.login(LoginRequest("ana@demo.cl", "otra")) }
        assertNotEquals("correcta", usuario.passwordHash)
    }

    @Test
    fun `login correcto devuelve el rol`() {
        val hash = BCryptPasswordEncoder().encode("demo123")
        val usuario = Usuario(1, Rol(1, "TURISTA", ""), "Ana", "ana@demo.cl", hash)
        `when`(usuarioRepo.findByEmailIgnoreCase("ana@demo.cl")).thenReturn(usuario)

        val respuesta = service.login(LoginRequest("ana@demo.cl", "demo123"))
        assertEquals("TURISTA", respuesta.rol)
    }
}
