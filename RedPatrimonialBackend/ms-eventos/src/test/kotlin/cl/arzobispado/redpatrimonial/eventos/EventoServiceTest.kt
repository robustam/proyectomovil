package cl.arzobispado.redpatrimonial.eventos

import cl.arzobispado.redpatrimonial.eventos.config.ReglaNegocioException
import cl.arzobispado.redpatrimonial.eventos.dto.InscripcionRequest
import cl.arzobispado.redpatrimonial.eventos.model.EventoCultural
import cl.arzobispado.redpatrimonial.eventos.repository.CategoriaEventoRepository
import cl.arzobispado.redpatrimonial.eventos.repository.EventoRepository
import cl.arzobispado.redpatrimonial.eventos.repository.InscripcionRepository
import cl.arzobispado.redpatrimonial.eventos.service.EventoService
import cl.arzobispado.redpatrimonial.eventos.service.NodosClient
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.Optional

/** Pruebas de las reglas de inscripción a eventos. */
class EventoServiceTest {

    private val eventoRepo = mock(EventoRepository::class.java)
    private val inscripcionRepo = mock(InscripcionRepository::class.java)
    private val service = EventoService(eventoRepo, mock(CategoriaEventoRepository::class.java), inscripcionRepo, mock(NodosClient::class.java))

    private fun evento(cupo: Int, estado: String = "PROGRAMADO") = EventoCultural(
        idEvento = 1, titulo = "Concierto", cupoMaximo = cupo, estado = estado,
        fechaInicio = LocalDateTime.now().plusDays(1), fechaFin = LocalDateTime.now().plusDays(1).plusHours(2)
    )

    @Test
    fun `no permite inscribirse si no quedan cupos`() {
        `when`(eventoRepo.findById(1L)).thenReturn(Optional.of(evento(cupo = 10)))
        `when`(inscripcionRepo.personasInscritas(1L)).thenReturn(9L)
        assertThrows<ReglaNegocioException> { service.inscribir(1, InscripcionRequest(idUsuario = 1, cantidadPersonas = 2)) }
    }

    @Test
    fun `no permite inscribirse en eventos cancelados`() {
        `when`(eventoRepo.findById(1L)).thenReturn(Optional.of(evento(cupo = 10, estado = "CANCELADO")))
        assertThrows<ReglaNegocioException> { service.inscribir(1, InscripcionRequest(idUsuario = 1, cantidadPersonas = 1)) }
    }

    @Test
    fun `el codigo QR tiene el formato esperado`() {
        val codigo = service.generarCodigo()
        assertTrue(Regex("PAS-\\d{4}-\\d{5}").matches(codigo), "Código generado: $codigo")
    }
}
