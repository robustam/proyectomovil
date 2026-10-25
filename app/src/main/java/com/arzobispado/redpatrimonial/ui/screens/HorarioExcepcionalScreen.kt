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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.arzobispado.redpatrimonial.data.model.EstadosApertura
import com.arzobispado.redpatrimonial.ui.components.*
import com.arzobispado.redpatrimonial.util.Formatos
import com.arzobispado.redpatrimonial.viewmodel.AuthViewModel
import com.arzobispado.redpatrimonial.viewmodel.NodoViewModel
import java.time.LocalDate
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.arzobispado.redpatrimonial.ui.preview.VistaPrevia
import com.arzobispado.redpatrimonial.ui.preview.rememberViewModelsPrueba
import com.arzobispado.redpatrimonial.data.model.Roles

/** CU11: bloques horarios especiales (un templo cerrado que abre, un cierre por celebración…). */
@Composable
fun HorarioExcepcionalScreen(navController: NavHostController, authVM: AuthViewModel, nodoVM: NodoViewModel) {
    val usuario by authVM.usuario.collectAsState()
    val nodos by nodoVM.nodos.collectAsState()
    val nodo = nodos.firstOrNull { it.idNodo == usuario?.idNodoAsignado }
    var fecha by remember { mutableStateOf(LocalDate.now().plusDays(1).toString()) }
    var inicio by remember { mutableStateOf("10:00") }
    var fin by remember { mutableStateOf("13:00") }
    var estado by remember { mutableStateOf(EstadosApertura.ABIERTO) }
    var motivo by remember { mutableStateOf("") }

    Pantalla(titulo = "Horarios excepcionales", subtitulo = nodo?.nombreOficial, onVolver = { navController.popBackStack() }, viewModels = listOf(nodoVM)) {
        if (nodo == null) {
            MensajeVacio("No tienes un templo asignado")
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Tarjeta {
                        TituloSeccion("Nuevo bloque especial")
                        Spacer(Modifier.height(8.dp))
                        CampoFormulario(fecha, { fecha = it }, "Fecha", icono = Icons.Default.CalendarMonth,
                            ayuda = if (Formatos.esFechaValida(fecha)) Formatos.fechaLarga(fecha) else "Formato AAAA-MM-DD")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CampoFormulario(inicio, { inicio = it }, "Desde (HH:MM)", Modifier.weight(1f))
                            CampoFormulario(fin, { fin = it }, "Hasta (HH:MM)", Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("Estado durante el bloque", fontSize = 12.sp, color = Paleta.TextoSuave)
                        SelectorOpciones(EstadosApertura.TODOS, estado, EstadosApertura::nombreVisible, { estado = it })
                        Spacer(Modifier.height(6.dp))
                        CampoFormulario(motivo, { motivo = it }, "Motivo (ej: jornada patrimonial)")
                        Spacer(Modifier.height(10.dp))
                        BotonPrincipal("Publicar horario", icono = Icons.Default.Save, onClick = {
                            nodoVM.agregarHorario(nodo.idNodo, fecha, inicio, fin, estado, motivo) { motivo = "" }
                        })
                    }
                }
                item { TituloSeccion("Próximos horarios especiales") }
                items(nodo.horariosExcepcionales, key = { it.idHorarioExc }) { h ->
                    Tarjeta {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(Formatos.fechaLarga(h.fecha), fontWeight = FontWeight.Bold)
                                Text("${h.horaInicio} - ${h.horaFin} · ${EstadosApertura.nombreVisible(h.estadoApertura)}", fontSize = 13.sp, color = Paleta.TextoMedio)
                                if (h.motivo.isNotBlank()) Text(h.motivo, fontSize = 12.sp, color = Paleta.TextoSuave)
                            }
                            IconButton(onClick = { nodoVM.eliminarHorario(nodo.idNodo, h.idHorarioExc) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Paleta.TextoSuave)
                            }
                        }
                    }
                }
                if (nodo.horariosExcepcionales.isEmpty()) item { MensajeVacio("No hay horarios especiales programados") }
            }
        }
    }
}


// ---------- Vista previa para el panel Split de Android Studio (usa datos de prueba) ----------
@Preview(name = "Horarios excepcionales", showBackground = true, showSystemUi = true)
@Composable
private fun HorarioExcepcionalScreenPreview() {
    val vm = rememberViewModelsPrueba(rol = Roles.ENCARGADO)
    VistaPrevia { HorarioExcepcionalScreen(rememberNavController(), vm.auth, vm.nodo) }
}
