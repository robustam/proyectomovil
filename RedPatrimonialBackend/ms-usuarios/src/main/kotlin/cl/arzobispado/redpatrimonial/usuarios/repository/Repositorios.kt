package cl.arzobispado.redpatrimonial.usuarios.repository

import cl.arzobispado.redpatrimonial.usuarios.model.PermisoNodo
import cl.arzobispado.redpatrimonial.usuarios.model.Rol
import cl.arzobispado.redpatrimonial.usuarios.model.Usuario
import org.springframework.data.jpa.repository.JpaRepository

interface RolRepository : JpaRepository<Rol, Long> {
    fun findByNombreRol(nombreRol: String): Rol?
}

interface UsuarioRepository : JpaRepository<Usuario, Long> {
    fun findByEmailIgnoreCase(email: String): Usuario?
    fun existsByEmailIgnoreCase(email: String): Boolean
}

interface PermisoNodoRepository : JpaRepository<PermisoNodo, Long> {
    fun findByUsuarioIdUsuario(idUsuario: Long): List<PermisoNodo>
}
