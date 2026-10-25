package com.arzobispado.redpatrimonial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.arzobispado.redpatrimonial.data.model.Evento
import com.arzobispado.redpatrimonial.navigation.AppScreens
import com.arzobispado.redpatrimonial.ui.components.*
import com.arzobispado.redpatrimonial.util.Formatos
import com.arzobispado.redpatrimonial.viewmodel.AuthViewModel
import com.arzobispado.redpatrimonial.viewmodel.EventoViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.arzobispado.redpatrimonial.ui.preview.VistaPrevia
import com.arzobispado.redpatrimonial.ui.preview.rememberViewModelsPrueba
import com.arzobispado.redpatrimonial.data.model.Roles

/** CU05: oferta cultural y litúrgica con reserva de cupo y ticket QR. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(navController: NavHostController, authVM: AuthViewModel, eventoVM: EventoViewModel) {
    val eventos by eventoVM.eventos.collectAsState()
    val usuario by authVM.usuario.collectAsState()
    var diaFiltro by remember { mutableStateOf<String?>(null) }
    var eventoReserva by remember { mutableStateOf<Evento?>(null) }
    var mostrarInvitado by remember { mutableStateOf(false) }

    val dias = eventos.map { it.fechaInicio.take(10) }.distinct().sorted()
    val visibles = eventos.filter { diaFiltro == null || it.fechaInicio.startsWith(diaFiltro!!) }

    Pantalla(
        titulo = "Oferta cultural y litúrgica",
        onVolver = { navController.popBackStack() },
        viewModels = listOf(eventoVM)
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { FilterChip(selected = diaFiltro == null, onClick = { diaFiltro = null }, label = { Text("Todos") }) }
            items(dias) { d ->
                FilterChip(selected = diaFiltro == d, onClick = { diaFiltro = d }, label = { Text(Formatos.fechaCorta(d)) })
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(visibles, key = { it.idEvento }) { e ->
                TarjetaEvento(
                    evento = e,
                    onTemplo = { navController.navigate(AppScreens.Detail.createRoute(e.idNodo)) },
                    onReservar = { if (usuario == null) mostrarInvitado = true else eventoReserva = e }
                )
            }
            if (visibles.isEmpty()) item { MensajeVacio("No hay eventos programados", Icons.Default.EventBusy) }
        }
    }

    eventoReserva?.let { e ->
        DialogoReserva(
            evento = e,
            onConfirmar = { cantidad ->
                usuario?.let { u ->
                    eventoVM.inscribir(e, u, cantidad) {
                        eventoReserva = null
                        navController.navigate(AppScreens.Ticket.route)
                    }
                }
            },
            onCerrar = { eventoReserva = null }
        )
    }
    if (mostrarInvitado) {
        DialogoInvitado(
            "Debes iniciar sesión para reservar cupos o inscribirte en eventos patrimoniales.",
            onIniciarSesion = { mostrarInvitado = false; navController.navigate(AppScreens.Login.route) },
            onCerrar = { mostrarInvitado = false }
        )
    }
}

@Composable
private fun TarjetaEvento(evento: Evento, onTemplo: () -> Unit, onReservar: () -> Unit) {
    val cancelado = evento.estado == "CANCELADO"
    Tarjeta {
        Row(verticalAlignment = Alignment.Top) {
            // Fecha tipo calendario
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(Paleta.AzulMuySuave, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(Formatos.fechaCorta(evento.fechaInicio).substringBefore(' ').uppercase(), fontSize = 11.sp, color = Paleta.Azul, fontWeight = FontWeight.Bold)
                Text(evento.fechaInicio.substring(8, 10), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Paleta.Texto)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Etiqueta(Formatos.categoriaEvento(evento.categoria))
                    if (cancelado) Etiqueta("Cancelado", Paleta.RojoSuave, Paleta.RojoOscuro)
                }
                Spacer(Modifier.height(4.dp))
                Text(evento.titulo, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Paleta.Texto)
                TextButton(onClick = onTemplo, contentPadding = PaddingValues(0.dp)) {
                    Icon(Icons.Default.Church, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(evento.nombreNodo, fontSize = 12.sp)
                }
                FilaDato(Icons.Default.Schedule, "${Formatos.soloHora(evento.fechaInicio)} - ${Formatos.soloHora(evento.fechaFin)} hrs", tinte = Paleta.Verde)
                if (evento.descripcion.isNotBlank()) Text(evento.descripcion, fontSize = 12.sp, color = Paleta.TextoSuave)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                when {
                    !evento.requiereInscripcion -> "Entrada libre, sin reserva"
                    evento.cupoMaximo > 0 -> "${evento.cuposDisponibles} de ${evento.cupoMaximo} cupos disponibles"
                    else -> "Requiere inscripción"
                },
                fontSize = 12.sp, color = Paleta.TextoMedio, modifier = Modifier.weight(1f)
            )
            if (evento.requiereInscripcion && !cancelado) {
                Button(
                    onClick = onReservar,
                    enabled = evento.cupoMaximo == 0 || evento.cuposDisponibles > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = Paleta.Azul),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reservar cupo", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun DialogoReserva(evento: Evento, onConfirmar: (Int) -> Unit, onCerrar: () -> Unit) {
    var cantidad by remember { mutableIntStateOf(1) }
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Reservar cupo") },
        text = {
            Column {
                Text(evento.titulo, fontWeight = FontWeight.Bold)
                Text("${evento.nombreNodo} · ${Formatos.fechaHora(evento.fechaInicio)}", fontSize = 13.sp, color = Paleta.TextoSuave)
                Spacer(Modifier.height(16.dp))
                Text("¿Cuántas personas asistirán?")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (cantidad > 1) cantidad-- }) { Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Menos") }
                    Text("$cantidad", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { if (cantidad < 10) cantidad++ }) { Icon(Icons.Default.AddCircleOutline, contentDescription = "Más") }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirmar(cantidad) }) { Text("Confirmar reserva") } },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } }
    )
}


// ---------- Vista previa para el panel Split de Android Studio (usa datos de prueba) ----------
@Preview(name = "Agenda cultural", showBackground = true, showSystemUi = true)
@Composable
private fun EventListScreenPreview() {
    val vm = rememberViewModelsPrueba(rol = Roles.TURISTA)
    VistaPrevia { EventListScreen(rememberNavController(), vm.auth, vm.evento) }
}
