package cl.arzobispado.redpatrimonial.usuarios.service

import cl.arzobispado.redpatrimonial.usuarios.config.NoEncontradoException
import cl.arzobispado.redpatrimonial.usuarios.config.ReglaNegocioException
import cl.arzobispado.redpatrimonial.usuarios.dto.*
import cl.arzobispado.redpatrimonial.usuarios.model.PermisoNodo
import cl.arzobispado.redpatrimonial.usuarios.model.Usuario
import cl.arzobispado.redpatrimonial.usuarios.repository.PermisoNodoRepository
import cl.arzobispado.redpatrimonial.usuarios.repository.RolRepository
import cl.arzobispado.redpatrimonial.usuarios.repository.UsuarioRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UsuarioService(
    private val usuarioRepository: UsuarioRepository,
    private val rolRepository: RolRepository,
    private val permisoRepository: PermisoNodoRepository,
    private val integracion: IntegracionRutasClient
) {
    private val encoder = BCryptPasswordEncoder()

    companion object {
        /** Roles que una persona puede elegir al registrarse desde la app. */
        val ROLES_AUTOREGISTRO = setOf("TURISTA", "PROFESOR", "GUIA")
    }

    @Transactional
    fun registrar(req: RegistroRequest): UsuarioResponse {
        val rolNombre = req.rol.uppercase()
        if (rolNombre !in ROLES_AUTOREGISTRO) {
            throw ReglaNegocioException("El rol $rolNombre no se puede elegir al registrarse")
        }
        if (usuarioRepository.existsByEmailIgnoreCase(req.email)) {
            throw ReglaNegocioException("Ya existe una cuenta con el correo ${req.email}")
        }
        val rol = rolRepository.findByNombreRol(rolNombre)
            ?: throw NoEncontradoException("Rol $rolNombre no existe")

        val usuario = usuarioRepository.save(
            Usuario(
                rol = rol,
                nombreCompleto = req.nombreCompleto.trim(),
                email = req.email.trim().lowercase(),
                passwordHash = encoder.encode(req.password)
            )
        )
        // Comunicación entre microservicios: si es guía, se crea su perfil en ms-rutas
        if (rolNombre == "GUIA") {
            integracion.crearPerfilGuia(usuario.idUsuario!!, usuario.nombreCompleto)
        }
        return aRespuesta(usuario)
    }

    @Transactional(readOnly = true)
    fun login(req: LoginRequest): UsuarioResponse {
        val usuario = usuarioRepository.findByEmailIgnoreCase(req.email.trim())
            ?: throw ReglaNegocioException("Correo o contraseña incorrectos")
        if (!encoder.matches(req.password, usuario.passwordHash)) {
            throw ReglaNegocioException("Correo o contraseña incorrectos")
        }
        if (usuario.estado != "ACTIVO") {
            throw ReglaNegocioException("La cuenta está desactivada")
        }
        return aRespuesta(usuario)
    }

    @Transactional(readOnly = true)
    fun listar(): List<UsuarioResponse> = usuarioRepository.findAll().map { aRespuesta(it) }

    @Transactional(readOnly = true)
    fun buscar(id: Long): UsuarioResponse = aRespuesta(obtener(id))

    @Transactional
    fun cambiarEstado(id: Long, estado: String): UsuarioResponse {
        val nuevo = estado.uppercase()
        if (nuevo !in setOf("ACTIVO", "INACTIVO")) throw ReglaNegocioException("Estado inválido: $estado")
        val usuario = obtener(id)
        usuario.estado = nuevo
        return aRespuesta(usuarioRepository.save(usuario))
    }

    @Transactional(readOnly = true)
    fun roles(): List<RolResponse> =
        rolRepository.findAll().map { RolResponse(it.idRol!!, it.nombreRol, it.descripcion) }

    /** CU20: un administrador asigna un templo/nodo a un encargado. */
    @Transactional
    fun asignarPermiso(req: PermisoRequest): PermisoResponse {
        val usuario = obtener(req.idUsuario)
        if (usuario.rol.nombreRol != "ENCARGADO") {
            throw ReglaNegocioException("Solo se pueden asignar nodos a usuarios con rol ENCARGADO")
        }
        val permiso = permisoRepository.save(PermisoNodo(usuario = usuario, idNodo = req.idNodo))
        return aPermiso(permiso)
    }

    @Transactional(readOnly = true)
    fun permisosDe(idUsuario: Long): List<PermisoResponse> =
        permisoRepository.findByUsuarioIdUsuario(idUsuario).map { aPermiso(it) }

    private fun obtener(id: Long): Usuario =
        usuarioRepository.findById(id).orElseThrow { NoEncontradoException("Usuario $id no existe") }

    private fun aRespuesta(u: Usuario) = UsuarioResponse(
        idUsuario = u.idUsuario!!,
        nombreCompleto = u.nombreCompleto,
        email = u.email,
        rol = u.rol.nombreRol,
        estado = u.estado,
        fechaRegistro = u.fechaRegistro.toString(),
        idNodoAsignado = permisoRepository.findByUsuarioIdUsuario(u.idUsuario!!).firstOrNull()?.idNodo
    )

    private fun aPermiso(p: PermisoNodo) = PermisoResponse(
        idPermiso = p.idPermiso!!,
        idUsuario = p.usuario.idUsuario!!,
        nombreUsuario = p.usuario.nombreCompleto,
        idNodo = p.idNodo,
        fechaAsignacion = p.fechaAsignacion.toString()
    )
}
