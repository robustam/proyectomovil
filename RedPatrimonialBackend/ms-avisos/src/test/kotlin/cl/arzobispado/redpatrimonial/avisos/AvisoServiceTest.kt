package cl.arzobispado.redpatrimonial.avisos

import cl.arzobispado.redpatrimonial.avisos.config.ReglaNegocioException
import cl.arzobispado.redpatrimonial.avisos.dto.AvisoRequest
import cl.arzobispado.redpatrimonial.avisos.model.AvisoComunitario
import cl.arzobispado.redpatrimonial.avisos.repository.AvisoRepository
import cl.arzobispado.redpatrimonial.avisos.repository.CategoriaAvisoRepository
import cl.arzobispado.redpatrimonial.avisos.repository.ReporteAvisoRepository
import cl.arzobispado.redpatrimonial.avisos.service.AvisoService
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import java.time.LocalDateTime

/** Pruebas de vigencia y publicación de avisos. */
class AvisoServiceTest {

    private val service = AvisoService(
        mock(AvisoRepository::class.java),
        mock(CategoriaAvisoRepository::class.java),
        mock(ReporteAvisoRepository::class.java)
    )

    @Test
    fun `un aviso expirado no es vigente`() {
        val ahora = LocalDateTime.now()
        val aviso = AvisoComunitario(fechaExpiracion = ahora.minusMinutes(1))
        assertFalse(service.esVigente(aviso, ahora))
    }

    @Test
    fun `un aviso retirado no es vigente aunque no haya expirado`() {
        val ahora = LocalDateTime.now()
        val aviso = AvisoComunitario(fechaExpiracion = ahora.plusHours(5), estado = "RETIRADO")
        assertFalse(service.esVigente(aviso, ahora))
    }

    @Test
    fun `un aviso activo y dentro de plazo es vigente`() {
        val ahora = LocalDateTime.now()
        assertTrue(service.esVigente(AvisoComunitario(fechaExpiracion = ahora.plusHours(1)), ahora))
    }

    @Test
    fun `no se puede publicar con una categoria inexistente`() {
        val req = AvisoRequest(1, "Catedral", 4, "INVENTADA", "Título", "Mensaje", 24)
        assertThrows<ReglaNegocioException> { service.publicar(req) }
    }
}
