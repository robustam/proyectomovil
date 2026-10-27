package cl.arzobispado.redpatrimonial.eventos.config

import cl.arzobispado.redpatrimonial.eventos.model.CategoriaEvento
import cl.arzobispado.redpatrimonial.eventos.model.EventoCultural
import cl.arzobispado.redpatrimonial.eventos.repository.CategoriaEventoRepository
import cl.arzobispado.redpatrimonial.eventos.repository.EventoRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalTime

/** Calendario de eventos FICTICIOS (anexo, punto 4.4). Las fechas son relativas a hoy. */
@Component
class DatosIniciales(
    private val categoriaRepository: CategoriaEventoRepository,
    private val eventoRepository: EventoRepository
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (categoriaRepository.count() > 0) return

        val cat = listOf(
            "MISA" to "Celebraciones litúrgicas",
            "CONCIERTO" to "Música sacra y conciertos",
            "EXPOSICION" to "Exposiciones de arte sacro",
            "VISITA_GUIADA" to "Recorridos guiados",
            "TALLER" to "Talleres y actividades educativas"
        ).associate { (n, d) -> n to categoriaRepository.save(CategoriaEvento(nombreCategoria = n, descripcion = d)) }

        val hoy = LocalDate.now()
        data class E(val nodo: Long, val nombre: String, val lat: Double, val lng: Double, val cat: String, val titulo: String,
                     val desc: String, val dias: Long, val hora: Int, val duracion: Long, val cupo: Int, val inscripcion: Boolean)

        listOf(
            E(1, "Catedral Metropolitana de Santiago", -33.4378, -70.6505, "MISA", "Misa dominical solemne", "Celebración abierta a la comunidad.", 2, 12, 1, 0, false),
            E(4, "Iglesia de Santo Domingo", -33.4366, -70.6483, "CONCIERTO", "Concierto de órgano barroco", "Repertorio de música sacra. Entrada liberada con reserva.", 1, 19, 2, 120, true),
            E(2, "Iglesia de San Francisco", -33.4446, -70.6466, "EXPOSICION", "Exposición de arte sacro", "Muestra temporal con taller guiado.", 3, 11, 3, 40, true),
            E(6, "Templo Votivo de Maipú", -33.5097, -70.7660, "VISITA_GUIADA", "Recorrido patrimonial por el Templo Votivo", "Visita guiada para familias y grupos.", 4, 10, 2, 60, true),
            E(8, "Basílica de Lourdes", -33.4301, -70.6869, "MISA", "Celebración mariana", "Liturgia comunitaria en el santuario.", 5, 18, 1, 0, false),
            E(11, "Parroquia Sagrado Corazón (nodo ficticio)", -33.6100, -70.5760, "TALLER", "Taller de patrimonio para niños", "Actividad educativa en jornada de apertura especial.", 1, 10, 3, 30, true),
            E(9, "Iglesia San Vicente Ferrer de Los Dominicos", -33.4087, -70.5446, "CONCIERTO", "Coro de cámara", "Concierto de música coral.", 6, 20, 1, 80, true)
        ).forEach { e ->
            val inicio = hoy.plusDays(e.dias).atTime(LocalTime.of(e.hora, 0))
            eventoRepository.save(
                EventoCultural(
                    idNodo = e.nodo, nombreNodo = e.nombre, latitud = e.lat, longitud = e.lng,
                    categoria = cat.getValue(e.cat), titulo = e.titulo, descripcion = e.desc,
                    fechaInicio = inicio, fechaFin = inicio.plusHours(e.duracion),
                    cupoMaximo = e.cupo, requiereInscripcion = e.inscripcion, idPublicador = 4
                )
            )
        }
    }
}
