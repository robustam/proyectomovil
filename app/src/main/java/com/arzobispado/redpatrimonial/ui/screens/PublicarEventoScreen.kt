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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.arzobispado.redpatrimonial.data.mock.DatosPrueba
import com.arzobispado.redpatrimonial.ui.components.*
import com.arzobispado.redpatrimonial.util.Formatos
import com.arzobispado.redpatrimonial.viewmodel.AuthViewModel
import com.arzobispado.redpatrimonial.viewmodel.EventoViewModel
import com.arzobispado.redpatrimonial.viewmodel.NodoViewModel
import java.time.LocalDate
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.arzobispado.redpatrimonial.ui.preview.VistaPrevia
import com.arzobispado.redpatrimonial.ui.preview.rememberViewModelsPrueba
import com.arzobispado.redpatrimonial.data.model.Roles

/** CU12: el encargado publica un evento cultural o litúrgico en su templo. */
@Composable
fun PublicarEventoScreen(navController: NavHostController, authVM: AuthViewModel, nodoVM: NodoViewModel, eventoVM: EventoViewModel) {
    val usuario by authVM.usuario.collectAsState()
    val nodos by nodoVM.nodos.collectAsState()
    val eventosNodo by eventoVM.eventosNodo.collectAsState()
    val nodo = nodos.firstOrNull { it.idNodo == usuario?.idNodoAsignado }
    LaunchedEffect(nodo?.idNodo) { nodo?.let { eventoVM.cargarDeNodo(it.idNodo) } }

    var categoria by remember { mutableStateOf(DatosPrueba.categoriasEvento.first()) }
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(LocalDate.now().plusDays(3).toString()) }
    var inicio by remember { mutableStateOf("19:00") }
    var fin by remember { mutableStateOf("20:30") }
    var requiereInscripcion by remember { mutableStateOf(true) }
    var cupo by remember { mutableStateOf("50") }

    Pantalla(titulo = "Publicar evento", subtitulo = nodo?.nombreOficial, onVolver = { navController.popBackStack() }, viewModels = listOf(eventoVM)) {
        if (nodo == null || usuario == null) {
            MensajeVacio("No tienes un templo asignado")
        } else {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Categoría", fontSize = 12.sp, color = Paleta.TextoSuave)
                SelectorOpciones(DatosPrueba.categoriasEvento, categoria, Formatos::categoriaEvento, { categoria = it })
                CampoFormulario(titulo, { titulo = it }, "Título del evento", icono = Icons.Default.Event)
                CampoFormulario(descripcion, { descripcion = it }, "Descripción", lineas = 3)
                CampoFormulario(fecha, { fecha = it }, "Fecha", icono = Icons.Default.CalendarMonth,
                    ayuda = if (Formatos.esFechaValida(fecha)) Formatos.fechaLarga(fecha) else "Formato AAAA-MM-DD")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CampoFormulario(inicio, { inicio = it }, "Inicio (HH:MM)", Modifier.weight(1f))
                    CampoFormulario(fin, { fin = it }, "Término (HH:MM)", Modifier.weight(1f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Requiere reserva de cupo (QR)", modifier = Modifier.weight(1f))
                    Switch(checked = requiereInscripcion, onCheckedChange = { requiereInscripcion = it })
                }
                if (requiereInscripcion) {
                    CampoFormulario(cupo, { cupo = it.filter(Char::isDigit) }, "Cupo máximo (0 = sin límite)", teclado = KeyboardType.Number)
                }
                BotonPrincipal("Publicar evento", icono = Icons.Default.Campaign, onClick = {
                    eventoVM.publicar(nodo, usuario!!, categoria, titulo, descripcion, fecha, inicio, fin,
                        if (requiereInscripcion) cupo else "0", requiereInscripcion) {
                        titulo = ""
                        descripcion = ""
                    }
                })

                TituloSeccion("Próximos eventos de tu templo (${eventosNodo.size})", Modifier.padding(top = 8.dp))
                eventosNodo.forEach { e ->
                    Tarjeta {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(e.titulo, fontWeight = FontWeight.Bold)
                                Text("${Formatos.categoriaEvento(e.categoria)} · ${Formatos.fechaHora(e.fechaInicio)}", fontSize = 12.sp, color = Paleta.TextoSuave)
                                if (e.estado == "CANCELADO") Text("Cancelado", color = Paleta.Rojo, fontSize = 12.sp)
                            }
                            if (e.estado != "CANCELADO") {
                                TextButton(onClick = { eventoVM.cancelarEvento(e) }) { Text("Cancelar", color = Paleta.Rojo) }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ---------- Vista previa para el panel Split de Android Studio (usa datos de prueba) ----------
@Preview(name = "Publicar evento", showBackground = true, showSystemUi = true)
@Composable
private fun PublicarEventoScreenPreview() {
    val vm = rememberViewModelsPrueba(rol = Roles.ENCARGADO) {
        usuario?.idNodoAsignado?.let { evento.cargarDeNodo(it) }
    }
    VistaPrevia { PublicarEventoScreen(rememberNavController(), vm.auth, vm.nodo, vm.evento) }
}
