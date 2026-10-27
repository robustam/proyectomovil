package cl.arzobispado.redpatrimonial.rutas.config

import cl.arzobispado.redpatrimonial.rutas.model.*
import cl.arzobispado.redpatrimonial.rutas.repository.*
import cl.arzobispado.redpatrimonial.rutas.service.RutaService
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime

/**
 * Datos SINTÉTICOS: rutas recomendadas, guías voluntarios ficticios,
 * objetivos pedagógicos de ejemplo y dos salidas de prueba.
 * Los ids de nodo coinciden con los cargados en ms-nodos.
 */
@Component
class DatosIniciales(
    private val rutaRepository: RutaRepository,
    private val rutaService: RutaService,
    private val guiaRepository: GuiaRepository,
    private val disponibilidadRepository: DisponibilidadRepository,
    private val objetivoRepository: ObjetivoRepository,
    private val salidaRepository: SalidaRepository,
    private val asignacionRepository: AsignacionRepository
) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String?) {
        if (rutaRepository.count() > 0) return

        data class P(val id: Long, val nombre: String, val lat: Double, val lng: Double, val min: Int, val nota: String)

        fun ruta(nombre: String, desc: String, enfoque: String, paradas: List<P>): RutaPatrimonial {
            val r = RutaPatrimonial(nombreRuta = nombre, descripcion = desc, enfoque = enfoque)
            paradas.forEachIndexed { i, p ->
                r.paradas.add(ParadaRuta(ruta = r, idNodo = p.id, nombreNodo = p.nombre, latitud = p.lat, longitud = p.lng,
                    ordenSecuencia = i + 1, tiempoSugeridoMin = p.min, notasRecorrido = p.nota))
            }
            rutaService.recalcular(r)
            return rutaRepository.save(r)
        }

        val colonial = ruta("Ruta Santiago Colonial", "Recorrido a pie por los templos más antiguos del centro.", "Histórico", listOf(
            P(1, "Catedral Metropolitana de Santiago", -33.4378, -70.6505, 30, "Parada inicial frente a la Plaza de Armas"),
            P(4, "Iglesia de Santo Domingo", -33.4366, -70.6483, 25, "Observar la fachada de piedra"),
            P(2, "Iglesia de San Francisco", -33.4446, -70.6466, 30, "Cierre de ruta y museo")
        ))
        ruta("Ruta Neogótica y Barrio Yungay", "Templos del sector poniente y el barrio Yungay.", "Comunitario", listOf(
            P(3, "Basílica del Salvador", -33.4399, -70.6620, 20, "Vista exterior (en restauración)"),
            P(5, "Parroquia San Saturnino", -33.4392, -70.6743, 25, "Corazón del barrio Yungay"),
            P(8, "Basílica de Lourdes", -33.4301, -70.6869, 30, "Gruta y santuario")
        ))
        val maipu = ruta("Ruta Patrimonial de Maipú", "Templo Votivo y Museo del Carmen: ideal para cursos escolares.", "Pedagógico / Escolar", listOf(
            P(6, "Templo Votivo de Maipú", -33.5097, -70.7660, 40, "Explicación histórica del santuario"),
            P(7, "Museo del Carmen de Maipú", -33.5103, -70.7668, 45, "Recorrido por las salas del museo")
        ))

        // Guía 1 = usuario demo guia@demo.cl (id 3 en ms-usuarios). Los otros son ficticios.
        val guiaDemo = guiaRepository.save(GuiaVoluntario(idUsuario = 3, nombre = "Guía Demo", especialidad = "Historia del arte colonial",
            idiomas = "Español, Inglés", calificacion = 4.9, cantidadEvaluaciones = 10, guiadosRealizados = 12))
        guiaRepository.save(GuiaVoluntario(idUsuario = 101, nombre = "Carlos Mendoza (ficticio)", especialidad = "Especialista histórico",
            idiomas = "Español", calificacion = 4.7, cantidadEvaluaciones = 6, guiadosRealizados = 8))
        guiaRepository.save(GuiaVoluntario(idUsuario = 102, nombre = "Valentina Rojas (ficticia)", especialidad = "Arte sacro y música",
            idiomas = "Español, Portugués", calificacion = 4.8, cantidadEvaluaciones = 4, guiadosRealizados = 5))

        val hoy = LocalDate.now()
        (1L..5L).forEach { d ->
            disponibilidadRepository.save(DisponibilidadGuia(guia = guiaDemo, fecha = hoy.plusDays(d),
                horaInicio = LocalTime.of(9, 0), horaFin = LocalTime.of(13, 0)))
        }

        val objetivos = listOf(
            ObjetivoPedagogico(codigoCurricular = "OA 02", descripcion = "Valoración de la arquitectura colonial", asignatura = "Historia"),
            ObjetivoPedagogico(codigoCurricular = "OA 05", descripcion = "Arte sacro y patrimonio vivo", asignatura = "Artes Visuales"),
            ObjetivoPedagogico(codigoCurricular = "OA 08", descripcion = "Identidad y patrimonio local", asignatura = "Historia"),
            ObjetivoPedagogico(codigoCurricular = "OA 11", descripcion = "Lectura de fuentes históricas", asignatura = "Lenguaje")
        ).map { objetivoRepository.save(it) }

        // Salida 1: pendiente de confirmación por el templo y por el guía (profesor demo id 2)
        val s1 = salidaRepository.save(SalidaPedagogica(idProfesor = 2, nombreProfesor = "Profesora Demo",
            colegio = "Colegio Parroquial Santa Teresa (ficticio)", curso = "8° Básico A", cantidadAlumnos = 35,
            ruta = colonial, fechaProgramada = hoy.plusDays(1), horaInicio = LocalTime.of(10, 0),
            notasGuia = "Dos estudiantes requieren accesibilidad", objetivos = mutableSetOf(objetivos[0], objetivos[1])))
        asignacionRepository.save(AsignacionGuia(guia = guiaDemo, salida = s1))

        // Salida 2: ya confirmada y aceptada
        val s2 = salidaRepository.save(SalidaPedagogica(idProfesor = 2, nombreProfesor = "Profesora Demo",
            colegio = "Colegio Parroquial Santa Teresa (ficticio)", curso = "6° Básico B", cantidadAlumnos = 30,
            ruta = maipu, fechaProgramada = hoy.plusDays(4), horaInicio = LocalTime.of(9, 30),
            estadoSalida = "CONFIRMADA", objetivos = mutableSetOf(objetivos[2])))
        asignacionRepository.save(AsignacionGuia(guia = guiaDemo, salida = s2, estadoConfirmacion = "ACEPTADA"))
    }
}
