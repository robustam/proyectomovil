package cl.arzobispado.redpatrimonial.avisos.config

import cl.arzobispado.redpatrimonial.avisos.model.AvisoComunitario
import cl.arzobispado.redpatrimonial.avisos.model.CategoriaAviso
import cl.arzobispado.redpatrimonial.avisos.repository.AvisoRepository
import cl.arzobispado.redpatrimonial.avisos.repository.CategoriaAvisoRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/** Avisos FICTICIOS de ejemplo publicados por el encargado demo (usuario 4). */
@Component
class DatosIniciales(
    private val categoriaRepository: CategoriaAvisoRepository,
    private val avisoRepository: AvisoRepository
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (categoriaRepository.count() > 0) return
        val cat = listOf(
            "CIERRE_TEMPORAL" to "⚠️", "APERTURA_ESPECIAL" to "📢", "CELEBRACION" to "⛪",
            "HORARIOS" to "🕒", "CONSERVACION" to "🛠️"
        ).associate { (n, i) -> n to categoriaRepository.save(CategoriaAviso(nombreCat = n, icono = i)) }

        val ahora = LocalDateTime.now()
        fun aviso(nodo: Long, nombre: String, lat: Double, lng: Double, c: String, titulo: String, msg: String, haceMin: Long, horas: Long) =
            avisoRepository.save(AvisoComunitario(idNodo = nodo, nombreNodo = nombre, idUsuario = 4, categoria = cat.getValue(c),
                titulo = titulo, mensaje = msg, fechaPublicacion = ahora.minusMinutes(haceMin),
                fechaExpiracion = ahora.plusHours(horas), latitud = lat, longitud = lng))

        aviso(5, "Parroquia San Saturnino", -33.4392, -70.6743, "CIERRE_TEMPORAL", "Cierre temporal por celebración",
            "Misa solemne en curso. Visitas turísticas suspendidas hasta las 13:00 hrs.", 12, 3)
        aviso(1, "Catedral Metropolitana de Santiago", -33.4378, -70.6505, "APERTURA_ESPECIAL", "Apertura especial de cripta",
            "Apertura excepcional de la cripta por jornada cultural.", 45, 24)
        aviso(11, "Parroquia Sagrado Corazón (nodo ficticio)", -33.6100, -70.5760, "APERTURA_ESPECIAL", "Templo abierto mañana",
            "Abriremos de 10:00 a 13:00 hrs por jornada patrimonial. ¡Los esperamos!", 90, 36)
        aviso(3, "Basílica del Salvador", -33.4399, -70.6620, "CONSERVACION", "Trabajos de restauración",
            "El acceso principal permanece cerrado por obras de conservación.", 300, 72)
    }
}
