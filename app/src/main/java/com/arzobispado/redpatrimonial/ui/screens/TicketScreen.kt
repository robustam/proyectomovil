package com.arzobispado.redpatrimonial.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.arzobispado.redpatrimonial.navigation.AppScreens
import com.arzobispado.redpatrimonial.ui.components.*
import com.arzobispado.redpatrimonial.util.Formatos
import com.arzobispado.redpatrimonial.viewmodel.EventoViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.arzobispado.redpatrimonial.ui.preview.VistaPrevia
import com.arzobispado.redpatrimonial.ui.preview.rememberViewModelsPrueba
import com.arzobispado.redpatrimonial.data.model.Roles
import com.arzobispado.redpatrimonial.data.model.Inscripcion
import java.time.LocalDate
import java.time.LocalDateTime

/** Confirmación de reserva con código QR (ticket de ingreso). */
@Composable
fun TicketScreen(navController: NavHostController, eventoVM: EventoViewModel) {
    val ticket by eventoVM.ticket.collectAsState()

    Pantalla(titulo = "Confirmación de ticket", onVolver = { navController.popBackStack() }, viewModels = listOf(eventoVM)) {
        val t = ticket
        if (t == null) {
            MensajeVacio("No hay ticket para mostrar")
        } else {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Paleta.Verde, modifier = Modifier.size(48.dp))
                Text(
                    if (t.estadoAsistencia == "CANCELADA") "Reserva cancelada" else "¡Reserva confirmada!",
                    fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Paleta.Texto
                )
                Spacer(Modifier.height(12.dp))
                Tarjeta {
                    Text(t.tituloEvento, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Paleta.Texto)
                    Text(t.nombreNodo, color = Paleta.TextoSuave)
                    Spacer(Modifier.height(8.dp))
                    FilaDato(Icons.Default.CalendarMonth, Formatos.fechaLarga(t.fechaEvento))
                    FilaDato(Icons.Default.Schedule, "${Formatos.soloHora(t.fechaEvento)} hrs")
                    FilaDato(Icons.Default.Groups, "${t.cantidadPersonas} persona(s)")
                    Spacer(Modifier.height(16.dp))
                    QrSimulado(t.codigoQr, Modifier.size(200.dp).align(Alignment.CenterHorizontally))
                    Spacer(Modifier.height(8.dp))
                    Text("ID: ${t.codigoQr}", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally))
                    Text("Muestra este código al ingreso del recinto", fontSize = 12.sp, color = Paleta.TextoSuave,
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
                Spacer(Modifier.height(16.dp))
                BotonPrincipal("Ver mis entradas", onClick = {
                    navController.navigate(AppScreens.Perfil.route) { launchSingleTop = true }
                }, icono = Icons.Default.ConfirmationNumber)
                Spacer(Modifier.height(8.dp))
                if (t.estadoAsistencia == "CONFIRMADA") {
                    OutlinedButton(onClick = { eventoVM.cancelarInscripcion(t); navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancelar reserva", color = Paleta.Rojo)
                    }
                }
            }
        }
    }
}


// ---------- Vista previa para el panel Split de Android Studio (usa datos de prueba) ----------
@Preview(name = "Ticket QR", showBackground = true, showSystemUi = true)
@Composable
private fun TicketScreenPreview() {
    val vm = rememberViewModelsPrueba(rol = Roles.TURISTA) {
        evento.verTicket(
            Inscripcion(1, 2, 1, "Concierto de órgano barroco", "Iglesia de Santo Domingo",
                LocalDate.now().plusDays(1).atTime(19, 0).toString(), 2, LocalDateTime.now().toString(), "CONFIRMADA", "PAS-2026-48213")
        )
    }
    VistaPrevia { TicketScreen(rememberNavController(), vm.evento) }
}
