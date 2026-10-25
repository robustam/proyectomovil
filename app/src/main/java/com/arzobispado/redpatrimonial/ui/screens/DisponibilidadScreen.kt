package com.arzobispado.redpatrimonial.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.arzobispado.redpatrimonial.ui.components.*
import com.arzobispado.redpatrimonial.util.Formatos
import com.arzobispado.redpatrimonial.viewmodel.GuiaViewModel
import java.time.LocalDate
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.arzobispado.redpatrimonial.ui.preview.VistaPrevia
import com.arzobispado.redpatrimonial.ui.preview.rememberViewModelsPrueba
import com.arzobispado.redpatrimonial.data.model.Roles

/** CU16: el guía registra los bloques en que puede acompañar grupos. */
@Composable
fun DisponibilidadScreen(navController: NavHostController, guiaVM: GuiaViewModel) {
    val bloques by guiaVM.disponibilidad.collectAsState()
    var fecha by remember { mutableStateOf(LocalDate.now().plusDays(1).toString()) }
    var inicio by remember { mutableStateOf("09:00") }
    var fin by remember { mutableStateOf("13:00") }

    Pantalla(titulo = "Mi disponibilidad", onVolver = { navController.popBackStack() }, viewModels = listOf(guiaVM)) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Tarjeta {
                    TituloSeccion("Nuevo bloque disponible")
                    Spacer(Modifier.height(8.dp))
                    CampoFormulario(fecha, { fecha = it }, "Fecha", icono = Icons.Default.CalendarMonth,
                        ayuda = if (Formatos.esFechaValida(fecha)) Formatos.fechaLarga(fecha) else "Formato AAAA-MM-DD")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CampoFormulario(inicio, { inicio = it }, "Desde (HH:MM)", Modifier.weight(1f))
                        CampoFormulario(fin, { fin = it }, "Hasta (HH:MM)", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    BotonPrincipal("Agregar bloque", onClick = { guiaVM.agregarDisponibilidad(fecha, inicio, fin) {} }, icono = Icons.Default.Add)
                }
            }
            item { TituloSeccion("Bloques registrados (${bloques.size})") }
            items(bloques, key = { it.idDisponibilidad }) { b ->
                Tarjeta {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EventAvailable, contentDescription = null, tint = Paleta.Verde)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(Formatos.fechaLarga(b.fecha), fontWeight = FontWeight.Bold)
                            Text("${b.horaInicio} - ${b.horaFin} · ${b.estadoBloque.lowercase()}", color = Paleta.TextoSuave)
                        }
                        IconButton(onClick = { guiaVM.eliminarDisponibilidad(b) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Paleta.TextoSuave)
                        }
                    }
                }
            }
            if (bloques.isEmpty()) item { MensajeVacio("Aún no registras bloques disponibles") }
        }
    }
}


// ---------- Vista previa para el panel Split de Android Studio (usa datos de prueba) ----------
@Preview(name = "Disponibilidad del guía", showBackground = true, showSystemUi = true)
@Composable
private fun DisponibilidadScreenPreview() {
    val vm = rememberViewModelsPrueba(rol = Roles.GUIA) {
        usuario?.let { guia.cargar(it) }
    }
    VistaPrevia { DisponibilidadScreen(rememberNavController(), vm.guia) }
}
