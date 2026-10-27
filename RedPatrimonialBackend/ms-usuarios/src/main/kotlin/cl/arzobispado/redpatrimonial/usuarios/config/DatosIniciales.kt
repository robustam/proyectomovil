package cl.arzobispado.redpatrimonial.usuarios.config

import cl.arzobispado.redpatrimonial.usuarios.model.PermisoNodo
import cl.arzobispado.redpatrimonial.usuarios.model.Rol
import cl.arzobispado.redpatrimonial.usuarios.model.Usuario
import cl.arzobispado.redpatrimonial.usuarios.repository.PermisoNodoRepository
import cl.arzobispado.redpatrimonial.usuarios.repository.RolRepository
import cl.arzobispado.redpatrimonial.usuarios.repository.UsuarioRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

/**
 * Carga datos SINTÉTICOS (ficticios) la primera vez que se inicia el servicio,
 * tal como exige el anexo del Arzobispado. Todas las cuentas usan la clave demo123.
 */
@Component
class DatosIniciales(
    private val rolRepository: RolRepository,
    private val usuarioRepository: UsuarioRepository,
    private val permisoRepository: PermisoNodoRepository
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (rolRepository.count() > 0) return

        val roles = listOf(
            Rol(nombreRol = "TURISTA", descripcion = "Turista o visitante que explora la red"),
            Rol(nombreRol = "PROFESOR", descripcion = "Docente de colegio o delegación educacional"),
            Rol(nombreRol = "GUIA", descripcion = "Guía voluntario acreditado"),
            Rol(nombreRol = "ENCARGADO", descripcion = "Encargado de templo / nodo de la red"),
            Rol(nombreRol = "ADMIN", descripcion = "Administrador central de la plataforma")
        ).map { rolRepository.save(it) }.associateBy { it.nombreRol }

        val encoder = BCryptPasswordEncoder()
        val clave = encoder.encode("demo123")

        // El orden importa: los ids 1..5 se usan en los datos de prueba de los otros microservicios
        usuarioRepository.save(Usuario(rol = roles.getValue("TURISTA"), nombreCompleto = "Turista Demo", email = "turista@demo.cl", passwordHash = clave))
        usuarioRepository.save(Usuario(rol = roles.getValue("PROFESOR"), nombreCompleto = "Profesora Demo", email = "profesor@demo.cl", passwordHash = clave))
        usuarioRepository.save(Usuario(rol = roles.getValue("GUIA"), nombreCompleto = "Guía Demo", email = "guia@demo.cl", passwordHash = clave))
        val encargado = usuarioRepository.save(Usuario(rol = roles.getValue("ENCARGADO"), nombreCompleto = "Encargado Demo", email = "encargado@demo.cl", passwordHash = clave))
        usuarioRepository.save(Usuario(rol = roles.getValue("ADMIN"), nombreCompleto = "Administrador Demo", email = "admin@demo.cl", passwordHash = clave))

        // El encargado demo administra el nodo 1 (Catedral Metropolitana en ms-nodos)
        permisoRepository.save(PermisoNodo(usuario = encargado, idNodo = 1))
    }
}
