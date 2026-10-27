package cl.arzobispado.redpatrimonial.nodos.config

import cl.arzobispado.redpatrimonial.nodos.model.*
import cl.arzobispado.redpatrimonial.nodos.repository.*
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime

/**
 * Catálogo SINTÉTICO de templos para la simulación académica (anexo, punto 4.4).
 * Incluye templos periféricos (Maipú, Quinta Normal, Las Condes, Puente Alto, La Florida)
 * porque el anexo indica que el flujo hoy se concentra solo cerca de la Plaza de Armas.
 * Las coordenadas son de prueba y las reseñas son textos de ejemplo.
 */
@Component
class DatosIniciales(
    private val regionRepository: RegionRepository,
    private val comunaRepository: ComunaRepository,
    private val zonaRepository: ZonaPastoralRepository,
    private val decanatoRepository: DecanatoRepository,
    private val tipoRepository: TipoNodoRepository,
    private val nodoRepository: NodoRepository
) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String?) {
        if (nodoRepository.count() > 0) return

        val rm = regionRepository.save(Region(nombreRegion = "Región Metropolitana"))
        val comunas = listOf("Santiago", "Maipú", "Quinta Normal", "Las Condes", "Puente Alto", "La Florida")
            .associateWith { comunaRepository.save(Comuna(region = rm, nombreComuna = it)) }

        val zCentro = zonaRepository.save(ZonaPastoral(nombreZona = "Zona Centro", descripcion = "Templos del casco histórico"))
        val zOeste = zonaRepository.save(ZonaPastoral(nombreZona = "Zona Oeste", descripcion = "Maipú y Quinta Normal"))
        val zOriente = zonaRepository.save(ZonaPastoral(nombreZona = "Zona Oriente", descripcion = "Sector oriente"))
        val zSur = zonaRepository.save(ZonaPastoral(nombreZona = "Zona Sur", descripcion = "Puente Alto y La Florida"))
        val decanatos = mapOf(
            "Centro" to decanatoRepository.save(Decanato(zona = zCentro, nombreDecanato = "Decanato Centro")),
            "Oeste" to decanatoRepository.save(Decanato(zona = zOeste, nombreDecanato = "Decanato Oeste")),
            "Oriente" to decanatoRepository.save(Decanato(zona = zOriente, nombreDecanato = "Decanato Oriente")),
            "Sur" to decanatoRepository.save(Decanato(zona = zSur, nombreDecanato = "Decanato Sur"))
        )
        val tipos = listOf("TEMPLO", "MUSEO", "COLEGIO", "ARCHIVO")
            .associateWith { tipoRepository.save(TipoNodo(nombreTipo = it)) }

        data class Semilla(
            val nombre: String, val direccion: String, val comuna: String, val tipo: String, val decanato: String,
            val lat: Double, val lng: Double, val estilo: String, val anio: Int?, val estado: EstadoApertura, val resena: String
        )

        val semillas = listOf(
            Semilla("Catedral Metropolitana de Santiago", "Plaza de Armas s/n", "Santiago", "TEMPLO", "Centro",
                -33.4378, -70.6505, "Neoclásico", 1748, EstadoApertura.ABIERTO,
                "Principal templo de la arquidiócesis, ubicado frente a la Plaza de Armas. Ficha de prueba para el MVP."),
            Semilla("Iglesia de San Francisco", "Av. Libertador Bernardo O'Higgins 834", "Santiago", "TEMPLO", "Centro",
                -33.4446, -70.6466, "Colonial", 1618, EstadoApertura.ABIERTO,
                "Considerada una de las construcciones más antiguas de Santiago. Ficha de prueba para el MVP."),
            Semilla("Basílica del Salvador", "Huérfanos 1701", "Santiago", "TEMPLO", "Centro",
                -33.4399, -70.6620, "Neogótico", null, EstadoApertura.CERRADO,
                "Templo de estilo neogótico actualmente en proceso de restauración. Ficha de prueba para el MVP."),
            Semilla("Iglesia de Santo Domingo", "Santo Domingo 961", "Santiago", "TEMPLO", "Centro",
                -33.4366, -70.6483, "Neoclásico", null, EstadoApertura.ABIERTO,
                "Templo histórico del centro de Santiago. Ficha de prueba para el MVP."),
            Semilla("Parroquia San Saturnino", "Santo Domingo 2772, Barrio Yungay", "Santiago", "TEMPLO", "Centro",
                -33.4392, -70.6743, "Historicista", null, EstadoApertura.EN_CELEBRACION,
                "Parroquia emblemática del Barrio Yungay. Ficha de prueba para el MVP."),
            Semilla("Templo Votivo de Maipú", "Av. 5 de Abril s/n, Plaza del Templo", "Maipú", "TEMPLO", "Oeste",
                -33.5097, -70.7660, "Moderno", null, EstadoApertura.ABIERTO,
                "Gran santuario ubicado en la comuna de Maipú, fuera del circuito céntrico tradicional. Ficha de prueba para el MVP."),
            Semilla("Museo del Carmen de Maipú", "Junto al Templo Votivo, Maipú", "Maipú", "MUSEO", "Oeste",
                -33.5103, -70.7668, "Contemporáneo", null, EstadoApertura.ABIERTO,
                "Museo de historia y arte religioso. Ficha de prueba para el MVP."),
            Semilla("Basílica de Lourdes", "Sector Gruta de Lourdes", "Quinta Normal", "TEMPLO", "Oeste",
                -33.4301, -70.6869, "Historicista", null, EstadoApertura.ABIERTO,
                "Santuario mariano junto a la gruta de Lourdes. Ficha de prueba para el MVP."),
            Semilla("Iglesia San Vicente Ferrer de Los Dominicos", "Av. Apoquindo 9085", "Las Condes", "TEMPLO", "Oriente",
                -33.4087, -70.5446, "Colonial", null, EstadoApertura.ABIERTO,
                "Templo de estilo colonial en el sector oriente. Ficha de prueba para el MVP."),
            Semilla("Archivo Histórico del Arzobispado", "Santiago Centro (dirección simulada)", "Santiago", "ARCHIVO", "Centro",
                -33.4425, -70.6530, "", null, EstadoApertura.CERRADO,
                "Archivo documental de la arquidiócesis. Atención con cita previa. Ficha de prueba para el MVP."),
            Semilla("Parroquia Sagrado Corazón (nodo ficticio)", "Av. Concha y Toro 1200 (simulada)", "Puente Alto", "TEMPLO", "Sur",
                -33.6100, -70.5760, "Moderno", null, EstadoApertura.CERRADO,
                "Templo periférico ficticio que abre de forma especial en jornadas patrimoniales."),
            Semilla("Colegio Parroquial Santa Teresa (ficticio)", "Av. Vicuña Mackenna 7000 (simulada)", "La Florida", "COLEGIO", "Sur",
                -33.5220, -70.5990, "", null, EstadoApertura.ABIERTO,
                "Colegio ficticio de la red que organiza salidas pedagógicas.")
        )

        semillas.forEachIndexed { i, s ->
            val nodo = NodoPatrimonial(
                tipoNodo = tipos.getValue(s.tipo),
                decanato = decanatos.getValue(s.decanato),
                comuna = comunas.getValue(s.comuna),
                nombreOficial = s.nombre,
                direccion = s.direccion,
                latitud = BigDecimal.valueOf(s.lat),
                longitud = BigDecimal.valueOf(s.lng),
                resenaHistorica = s.resena,
                estiloArquitectonico = s.estilo,
                fechaConstruccion = s.anio,
                estadoOperativo = s.estado
            )
            // Horarios regulares según el tipo de nodo
            val (dias, abre, cierra) = when (s.tipo) {
                "MUSEO" -> Triple(2..7, LocalTime.of(10, 0), LocalTime.of(18, 0))
                "ARCHIVO", "COLEGIO" -> Triple(1..5, LocalTime.of(9, 30), LocalTime.of(17, 0))
                else -> Triple(1..7, LocalTime.of(9, 0), LocalTime.of(19, 0))
            }
            dias.forEach { d -> nodo.horariosRegulares.add(HorarioRegular(nodo = nodo, diaSemana = d, horaApertura = abre, horaCierre = cierra)) }

            // Galería multimedia simulada
            nodo.recursos.add(RecursoMultimedia(nodo = nodo, tipoRecurso = "IMAGEN", titulo = "Fachada principal", urlRecurso = "https://ejemplo.cl/nodos/${i + 1}/fachada.jpg", esPrincipal = true))
            nodo.recursos.add(RecursoMultimedia(nodo = nodo, tipoRecurso = "IMAGEN", titulo = "Vista interior", urlRecurso = "https://ejemplo.cl/nodos/${i + 1}/interior.jpg"))
            if (s.tipo == "TEMPLO") {
                nodo.recursos.add(RecursoMultimedia(nodo = nodo, tipoRecurso = "AUDIO", titulo = "Audioguía patrimonial", urlRecurso = "https://ejemplo.cl/nodos/${i + 1}/audioguia.mp3", duracionSegundos = 510))
            }
            nodoRepository.save(nodo)
        }

        // Horarios excepcionales de ejemplo (fechas relativas a hoy)
        val hoy = LocalDate.now()
        fun excepcional(idNodo: Long, dias: Long, ini: Int, fin: Int, estado: EstadoApertura, motivo: String) {
            val nodo = nodoRepository.findById(idNodo).orElseThrow()
            nodo.horariosExcepcionales.add(
                HorarioExcepcional(nodo = nodo, fecha = hoy.plusDays(dias), horaInicio = LocalTime.of(ini, 0),
                    horaFin = LocalTime.of(fin, 0), estadoApertura = estado, motivo = motivo)
            )
            nodoRepository.save(nodo)
        }
        excepcional(1, 2, 12, 14, EstadoApertura.EN_CELEBRACION, "Misa solemne: visitas turísticas suspendidas")
        excepcional(3, 3, 10, 12, EstadoApertura.ABIERTO, "Apertura especial para visitas guiadas")
        excepcional(11, 1, 10, 13, EstadoApertura.ABIERTO, "Apertura especial por jornada patrimonial")
    }
}
