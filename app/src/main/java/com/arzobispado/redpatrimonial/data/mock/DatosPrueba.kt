package com.arzobispado.redpatrimonial.data.mock

import com.arzobispado.redpatrimonial.data.model.*
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * DATOS DE PRUEBA SINTÉTICOS (iguales a los que cargan los microservicios).
 * Se usan cuando el backend no está encendido, para que la app nunca quede vacía.
 * Son listas en memoria: los cambios se pierden al cerrar la app.
 */
object DatosPrueba {

    private val hoy: LocalDate get() = LocalDate.now()

    /** Contraseña de todas las cuentas demo. */
    const val CLAVE_DEMO = "demo123"

    val usuarios = mutableListOf(
        Usuario(1, "Turista Demo", "turista@demo.cl", Roles.TURISTA),
        Usuario(2, "Profesora Demo", "profesor@demo.cl", Roles.PROFESOR),
        Usuario(3, "Guía Demo", "guia@demo.cl", Roles.GUIA),
        Usuario(4, "Encargado Demo", "encargado@demo.cl", Roles.ENCARGADO, idNodoAsignado = 1),
        Usuario(5, "Administrador Demo", "admin@demo.cl", Roles.ADMIN)
    )
    /** Claves de las cuentas creadas sin conexión (solo en memoria). */
    val claves = mutableMapOf<String, String>()

    private fun horariosTemplo() = (1..7).map { HorarioRegular(it, "09:00", "19:00") }
    private fun horariosMuseo() = (2..7).map { HorarioRegular(it, "10:00", "18:00") }
    private fun horariosOficina() = (1..5).map { HorarioRegular(it, "09:30", "17:00") }

    private fun galeria(id: Long, conAudio: Boolean): List<RecursoMultimedia> = buildList {
        add(RecursoMultimedia(id * 10 + 1, "IMAGEN", "Fachada principal", "https://ejemplo.cl/nodos/$id/fachada.jpg", esPrincipal = true))
        add(RecursoMultimedia(id * 10 + 2, "IMAGEN", "Vista interior", "https://ejemplo.cl/nodos/$id/interior.jpg"))
        if (conAudio) add(RecursoMultimedia(id * 10 + 3, "AUDIO", "Audioguía patrimonial", "https://ejemplo.cl/nodos/$id/audioguia.mp3", 510))
    }

    private fun nodo(
        id: Long, nombre: String, dir: String, comuna: String, tipo: String, decanato: String,
        lat: Double, lng: Double, estilo: String, anio: Int?, estado: String, resena: String
    ) = NodoPatrimonial(
        idNodo = id, nombreOficial = nombre, direccion = dir, comuna = comuna, tipoNodo = tipo,
        decanato = decanato, latitud = lat, longitud = lng, resenaHistorica = resena,
        estiloArquitectonico = estilo, anioConstruccion = anio, estadoOperativo = estado, estadoActual = estado,
        horariosRegulares = when (tipo) {
            "MUSEO" -> horariosMuseo()
            "ARCHIVO", "COLEGIO" -> horariosOficina()
            else -> horariosTemplo()
        },
        recursos = galeria(id, tipo == "TEMPLO")
    )

    val nodos: MutableList<NodoPatrimonial> by lazy {
        mutableListOf(
            nodo(1, "Catedral Metropolitana de Santiago", "Plaza de Armas s/n", "Santiago", "TEMPLO", "Decanato Centro",
                -33.4378, -70.6505, "Neoclásico", 1748, EstadosApertura.ABIERTO,
                "Principal templo de la arquidiócesis, ubicado frente a la Plaza de Armas. Ficha de prueba para el MVP.")
                .copy(horariosExcepcionales = listOf(
                    HorarioExcepcional(1, 1, hoy.plusDays(2).toString(), "12:00", "14:00", EstadosApertura.EN_CELEBRACION, "Misa solemne: visitas turísticas suspendidas")
                )),
            nodo(2, "Iglesia de San Francisco", "Av. Libertador Bernardo O'Higgins 834", "Santiago", "TEMPLO", "Decanato Centro",
                -33.4446, -70.6466, "Colonial", 1618, EstadosApertura.ABIERTO,
                "Considerada una de las construcciones más antiguas de Santiago. Ficha de prueba para el MVP."),
            nodo(3, "Basílica del Salvador", "Huérfanos 1701", "Santiago", "TEMPLO", "Decanato Centro",
                -33.4399, -70.6620, "Neogótico", null, EstadosApertura.CERRADO,
                "Templo de estilo neogótico actualmente en proceso de restauración. Ficha de prueba para el MVP.")
                .copy(horariosExcepcionales = listOf(
                    HorarioExcepcional(2, 3, hoy.plusDays(3).toString(), "10:00", "12:00", EstadosApertura.ABIERTO, "Apertura especial para visitas guiadas")
                )),
            nodo(4, "Iglesia de Santo Domingo", "Santo Domingo 961", "Santiago", "TEMPLO", "Decanato Centro",
                -33.4366, -70.6483, "Neoclásico", null, EstadosApertura.ABIERTO,
                "Templo histórico del centro de Santiago. Ficha de prueba para el MVP."),
            nodo(5, "Parroquia San Saturnino", "Santo Domingo 2772, Barrio Yungay", "Santiago", "TEMPLO", "Decanato Centro",
                -33.4392, -70.6743, "Historicista", null, EstadosApertura.EN_CELEBRACION,
                "Parroquia emblemática del Barrio Yungay. Ficha de prueba para el MVP."),
            nodo(6, "Templo Votivo de Maipú", "Av. 5 de Abril s/n, Plaza del Templo", "Maipú", "TEMPLO", "Decanato Oeste",
                -33.5097, -70.7660, "Moderno", null, EstadosApertura.ABIERTO,
                "Gran santuario ubicado en la comuna de Maipú, fuera del circuito céntrico tradicional. Ficha de prueba para el MVP."),
            nodo(7, "Museo del Carmen de Maipú", "Junto al Templo Votivo, Maipú", "Maipú", "MUSEO", "Decanato Oeste",
                -33.5103, -70.7668, "Contemporáneo", null, EstadosApertura.ABIERTO,
                "Museo de historia y arte religioso. Ficha de prueba para el MVP."),
            nodo(8, "Basílica de Lourdes", "Sector Gruta de Lourdes", "Quinta Normal", "TEMPLO", "Decanato Oeste",
                -33.4301, -70.6869, "Historicista", null, EstadosApertura.ABIERTO,
                "Santuario mariano junto a la gruta de Lourdes. Ficha de prueba para el MVP."),
            nodo(9, "Iglesia San Vicente Ferrer de Los Dominicos", "Av. Apoquindo 9085", "Las Condes", "TEMPLO", "Decanato Oriente",
                -33.4087, -70.5446, "Colonial", null, EstadosApertura.ABIERTO,
                "Templo de estilo colonial en el sector oriente. Ficha de prueba para el MVP."),
            nodo(10, "Archivo Histórico del Arzobispado", "Santiago Centro (dirección simulada)", "Santiago", "ARCHIVO", "Decanato Centro",
                -33.4425, -70.6530, "", null, EstadosApertura.CERRADO,
                "Archivo documental de la arquidiócesis. Atención con cita previa. Ficha de prueba para el MVP."),
            nodo(11, "Parroquia Sagrado Corazón (nodo ficticio)", "Av. Concha y Toro 1200 (simulada)", "Puente Alto", "TEMPLO", "Decanato Sur",
                -33.6100, -70.5760, "Moderno", null, EstadosApertura.CERRADO,
                "Templo periférico ficticio que abre de forma especial en jornadas patrimoniales.")
                .copy(horariosExcepcionales = listOf(
                    HorarioExcepcional(3, 11, hoy.plusDays(1).toString(), "10:00", "13:00", EstadosApertura.ABIERTO, "Apertura especial por jornada patrimonial")
                )),
            nodo(12, "Colegio Parroquial Santa Teresa (ficticio)", "Av. Vicuña Mackenna 7000 (simulada)", "La Florida", "COLEGIO", "Decanato Sur",
                -33.5220, -70.5990, "", null, EstadosApertura.ABIERTO,
                "Colegio ficticio de la red que organiza salidas pedagógicas.")
        )
    }

    private fun fechaHora(dias: Long, hora: Int) = hoy.plusDays(dias).atTime(hora, 0).toString()

    val eventos: MutableList<Evento> by lazy {
        mutableListOf(
            Evento(1, 1, "Catedral Metropolitana de Santiago", -33.4378, -70.6505, "MISA", "Misa dominical solemne",
                "Celebración abierta a la comunidad.", fechaHora(2, 12), fechaHora(2, 13), 0, 0, false, "PROGRAMADO"),
            Evento(2, 4, "Iglesia de Santo Domingo", -33.4366, -70.6483, "CONCIERTO", "Concierto de órgano barroco",
                "Repertorio de música sacra. Entrada liberada con reserva.", fechaHora(1, 19), fechaHora(1, 21), 120, 120, true, "PROGRAMADO"),
            Evento(3, 2, "Iglesia de San Francisco", -33.4446, -70.6466, "EXPOSICION", "Exposición de arte sacro",
                "Muestra temporal con taller guiado.", fechaHora(3, 11), fechaHora(3, 14), 40, 40, true, "PROGRAMADO"),
            Evento(4, 6, "Templo Votivo de Maipú", -33.5097, -70.7660, "VISITA_GUIADA", "Recorrido patrimonial por el Templo Votivo",
                "Visita guiada para familias y grupos.", fechaHora(4, 10), fechaHora(4, 12), 60, 60, true, "PROGRAMADO"),
            Evento(5, 8, "Basílica de Lourdes", -33.4301, -70.6869, "MISA", "Celebración mariana",
                "Liturgia comunitaria en el santuario.", fechaHora(5, 18), fechaHora(5, 19), 0, 0, false, "PROGRAMADO"),
            Evento(6, 11, "Parroquia Sagrado Corazón (nodo ficticio)", -33.6100, -70.5760, "TALLER", "Taller de patrimonio para niños",
                "Actividad educativa en jornada de apertura especial.", fechaHora(1, 10), fechaHora(1, 13), 30, 30, true, "PROGRAMADO"),
            Evento(7, 9, "Iglesia San Vicente Ferrer de Los Dominicos", -33.4087, -70.5446, "CONCIERTO", "Coro de cámara",
                "Concierto de música coral.", fechaHora(6, 20), fechaHora(6, 21), 80, 80, true, "PROGRAMADO")
        )
    }

    val inscripciones = mutableListOf<Inscripcion>()

    val categoriasEvento = listOf("MISA", "CONCIERTO", "EXPOSICION", "VISITA_GUIADA", "TALLER")

    private fun parada(n: Long, min: Int, nota: String, orden: Int): Parada {
        val nodo = nodos.first { it.idNodo == n }
        return Parada(n * 100 + orden, n, nodo.nombreOficial, nodo.latitud, nodo.longitud, orden, min, nota)
    }

    val rutas: MutableList<Ruta> by lazy {
        mutableListOf(
            Ruta(1, null, "Ruta Santiago Colonial", "Recorrido a pie por los templos más antiguos del centro.", 1.4, 104, "Baja", "Histórico", false,
                listOf(parada(1, 30, "Parada inicial frente a la Plaza de Armas", 1), parada(4, 25, "Observar la fachada de piedra", 2), parada(2, 30, "Cierre de ruta y museo", 3))),
            Ruta(2, null, "Ruta Neogótica y Barrio Yungay", "Templos del sector poniente y el barrio Yungay.", 3.2, 118, "Media", "Comunitario", false,
                listOf(parada(3, 20, "Vista exterior (en restauración)", 1), parada(5, 25, "Corazón del barrio Yungay", 2), parada(8, 30, "Gruta y santuario", 3))),
            Ruta(3, null, "Ruta Patrimonial de Maipú", "Templo Votivo y Museo del Carmen: ideal para cursos escolares.", 0.1, 86, "Baja", "Pedagógico / Escolar", false,
                listOf(parada(6, 40, "Explicación histórica del santuario", 1), parada(7, 45, "Recorrido por las salas del museo", 2)))
        )
    }

    val guias = mutableListOf(
        Guia(1, 3, "Guía Demo", "Historia del arte colonial", "Español, Inglés", "Acreditación Red Patrimonial", true, 4.9, 12),
        Guia(2, 101, "Carlos Mendoza (ficticio)", "Especialista histórico", "Español", "Acreditación Red Patrimonial", true, 4.7, 8),
        Guia(3, 102, "Valentina Rojas (ficticia)", "Arte sacro y música", "Español, Portugués", "Acreditación Red Patrimonial", true, 4.8, 5)
    )

    val disponibilidad: MutableList<Disponibilidad> by lazy {
        (1L..5L).map { d -> Disponibilidad(d, 1, hoy.plusDays(d).toString(), "09:00", "13:00", "DISPONIBLE") }.toMutableList()
    }

    val objetivos = listOf(
        ObjetivoPedagogico(1, "OA 02", "Valoración de la arquitectura colonial", "Historia"),
        ObjetivoPedagogico(2, "OA 05", "Arte sacro y patrimonio vivo", "Artes Visuales"),
        ObjetivoPedagogico(3, "OA 08", "Identidad y patrimonio local", "Historia"),
        ObjetivoPedagogico(4, "OA 11", "Lectura de fuentes históricas", "Lenguaje")
    )

    val salidas: MutableList<SalidaPedagogica> by lazy {
        val r1 = rutas[0]
        val r3 = rutas[2]
        mutableListOf(
            SalidaPedagogica(1, 2, "Profesora Demo", "Colegio Parroquial Santa Teresa (ficticio)", "8° Básico A", 35,
                r1.idRuta, r1.nombreRuta, r1.tiempoEstimadoMin, r1.paradas.map { it.idNodo }, r1.paradas.map { it.nombreNodo },
                hoy.plusDays(1).toString(), "10:00",
                listOf("OA 02 - Valoración de la arquitectura colonial", "OA 05 - Arte sacro y patrimonio vivo"),
                "Dos estudiantes requieren accesibilidad", EstadosSalida.PENDIENTE, 1, "Guía Demo", EstadosSalida.PENDIENTE),
            SalidaPedagogica(2, 2, "Profesora Demo", "Colegio Parroquial Santa Teresa (ficticio)", "6° Básico B", 30,
                r3.idRuta, r3.nombreRuta, r3.tiempoEstimadoMin, r3.paradas.map { it.idNodo }, r3.paradas.map { it.nombreNodo },
                hoy.plusDays(4).toString(), "09:30", listOf("OA 08 - Identidad y patrimonio local"),
                "", EstadosSalida.CONFIRMADA, 1, "Guía Demo", EstadosSalida.ACEPTADA)
        )
    }

    private fun hace(min: Long) = LocalDateTime.now().minusMinutes(min).toString()
    private fun dentroDe(horas: Long) = LocalDateTime.now().plusHours(horas).toString()

    val avisos: MutableList<Aviso> by lazy {
        mutableListOf(
            Aviso(1, 5, "Parroquia San Saturnino", 4, CategoriasAviso.CIERRE_TEMPORAL, "⚠️", "Cierre temporal por celebración",
                "Misa solemne en curso. Visitas turísticas suspendidas hasta las 13:00 hrs.", hace(12), dentroDe(3), 12, -33.4392, -70.6743, "ACTIVO", true),
            Aviso(2, 1, "Catedral Metropolitana de Santiago", 4, CategoriasAviso.APERTURA_ESPECIAL, "📢", "Apertura especial de cripta",
                "Apertura excepcional de la cripta por jornada cultural.", hace(45), dentroDe(24), 45, -33.4378, -70.6505, "ACTIVO", true),
            Aviso(3, 11, "Parroquia Sagrado Corazón (nodo ficticio)", 4, CategoriasAviso.APERTURA_ESPECIAL, "📢", "Templo abierto mañana",
                "Abriremos de 10:00 a 13:00 hrs por jornada patrimonial. ¡Los esperamos!", hace(90), dentroDe(36), 90, -33.6100, -70.5760, "ACTIVO", true),
            Aviso(4, 3, "Basílica del Salvador", 4, CategoriasAviso.CONSERVACION, "🛠️", "Trabajos de restauración",
                "El acceso principal permanece cerrado por obras de conservación.", hace(300), dentroDe(72), 300, -33.4399, -70.6620, "ACTIVO", true)
        )
    }

    /** Genera ids nuevos para lo que se crea sin conexión. */
    private var secuencia = 1000L
    fun nuevoId(): Long = ++secuencia
}
