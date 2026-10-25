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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.arzobispado.redpatrimonial.data.model.EstadosSalida
import com.arzobispado.redpatrimonial.data.model.SalidaPedagogica
import com.arzobispado.redpatrimonial.navigation.AppScreens
import com.arzobispado.redpatrimonial.ui.components.*
import com.arzobispado.redpatrimonial.viewmodel.AuthViewModel
import com.arzobispado.redpatrimonial.viewmodel.GuiaViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.arzobispado.redpatrimonial.ui.preview.VistaPrevia
import com.arzobispado.redpatrimonial.ui.preview.rememberViewModelsPrueba
import com.arzobispado.redpatrimonial.data.model.Roles

/** Panel del guía voluntario: aceptar, reagendar y finalizar recorridos (CU17, CU18). */
@Composable
fun PanelGuiaScreen(navController: NavHostController, authVM: AuthViewModel, guiaVM: GuiaViewModel) {
    val usuario by authVM.usuario.collectAsState()
    val guia by guiaVM.guia.collectAsState()
    val asignaciones by guiaVM.asignaciones.collectAsState()
    val disponibilidad by guiaVM.disponibilidad.collectAsState()
    // Diálogo para escribir un comentario: (salida, true = finalizar / false = reagendar)
    var dialogo by remember { mutableStateOf<Pair<SalidaPedagogica, Boolean>?>(null) }
    LaunchedEffect(usuario?.idUsuario) { usuario?.let { guiaVM.cargar(it) } }

    val proximas = asignaciones.filter { it.estadoAsignacion != EstadosSalida.FINALIZADA }
    val realizadas = asignaciones.filter { it.estadoAsignacion == EstadosSalida.FINALIZADA }

    Pantalla(
        titulo = "Hola, ${usuario?.nombreCompleto ?: "guía"}",
        subtitulo = "Panel de guía voluntario acreditado",
        viewModels = listOf(guiaVM, authVM)
    ) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Tarjeta {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(if (guia?.estadoActivo == true) "Estado: disponible para guiados" else "Estado: en pausa",
                                fontWeight = FontWeight.Bold, color = if (guia?.estadoActivo == true) Paleta.Verde else Paleta.TextoSuave)
                            Text(guia?.especialidad ?: "", fontSize = 12.sp, color = Paleta.TextoSuave)
                        }
                        Switch(checked = guia?.estadoActivo == true, onCheckedChange = { guiaVM.cambiarActivo(it) })
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Indicador("${guia?.guiadosRealizados ?: 0}", "Guiados realizados", Modifier.weight(1f))
                    Indicador("${guia?.calificacion ?: "-"} ★", "Calificación promedio", Modifier.weight(1f))
                    Indicador("${disponibilidad.size}", "Bloques disponibles", Modifier.weight(1f))
                }
            }
            item {
                OutlinedButton(onClick = { navController.navigate(AppScreens.Disponibilidad.route) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.EventAvailable, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Registrar mi disponibilidad horaria")
                }
            }
            item { TituloSeccion("Próximos recorridos asignados (${proximas.size})") }
            items(proximas, key = { it.idSalida }) { s ->
                TarjetaSalida(s) {
                    when (s.estadoAsignacion) {
                        EstadosSalida.PENDIENTE, EstadosSalida.REAGENDAR -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { guiaVM.responder(s, aceptar = true) }, colors = ButtonDefaults.buttonColors(containerColor = Paleta.Verde)) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Aceptar")
                            }
                            if (s.estadoAsignacion == EstadosSalida.PENDIENTE) {
                                OutlinedButton(onClick = { dialogo = s to false }) { Text("Reagendar") }
                            }
                        }
                        EstadosSalida.ACEPTADA -> Button(onClick = { dialogo = s to true }, colors = ButtonDefaults.buttonColors(containerColor = Paleta.Azul)) {
                            Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Registrar finalización")
                        }
                    }
                }
            }
            if (proximas.isEmpty()) item { MensajeVacio("No tienes recorridos pendientes", Icons.Default.EventAvailable) }
            if (realizadas.isNotEmpty()) {
                item { TituloSeccion("Recorridos realizados (${realizadas.size})") }
                items(realizadas, key = { "r${it.idSalida}" }) { s -> TarjetaSalida(s) }
            }
        }
    }

    dialogo?.let { (salida, finalizar) ->
        var comentario by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { dialogo = null },
            title = { Text(if (finalizar) "Registrar finalización" else "Solicitar reagendar") },
            text = {
                Column {
                    Text("${salida.curso} · ${salida.nombreRuta}", fontSize = 13.sp, color = Paleta.TextoSuave)
                    Spacer(Modifier.height(8.dp))
                    CampoFormulario(comentario, { comentario = it },
                        if (finalizar) "¿Cómo resultó el recorrido?" else "Motivo / fecha alternativa", lineas = 3)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (finalizar) guiaVM.finalizar(salida, comentario) else guiaVM.responder(salida, aceptar = false, comentario = comentario)
                    dialogo = null
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { dialogo = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
fun Indicador(valor: String, titulo: String, modifier: Modifier = Modifier) {
    Surface(color = Color.White, shape = MaterialTheme.shapes.large, shadowElevation = 1.dp, modifier = modifier) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(valor, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Paleta.Azul)
            Text(titulo, fontSize = 11.sp, color = Paleta.TextoSuave, lineHeight = 13.sp)
        }
    }
}


// ---------- Vista previa para el panel Split de Android Studio (usa datos de prueba) ----------
@Preview(name = "Panel del guía", showBackground = true, showSystemUi = true)
@Composable
private fun PanelGuiaScreenPreview() {
    val vm = rememberViewModelsPrueba(rol = Roles.GUIA) {
        usuario?.let { guia.cargar(it) }
    }
    VistaPrevia { PanelGuiaScreen(rememberNavController(), vm.auth, vm.guia) }
}
