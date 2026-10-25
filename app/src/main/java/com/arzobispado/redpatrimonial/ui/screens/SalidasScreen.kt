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
import com.arzobispado.redpatrimonial.util.Formatos
import com.arzobispado.redpatrimonial.viewmodel.AuthViewModel
import com.arzobispado.redpatrimonial.viewmodel.SalidaViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.arzobispado.redpatrimonial.ui.preview.VistaPrevia
import com.arzobispado.redpatrimonial.ui.preview.rememberViewModelsPrueba
import com.arzobispado.redpatrimonial.data.model.Roles

/** Salidas pedagógicas del profesor con su estado de confirmación (CU07, CU10). */
@Composable
fun SalidasScreen(navController: NavHostController, authVM: AuthViewModel, salidaVM: SalidaViewModel) {
    val usuario by authVM.usuario.collectAsState()
    val salidas by salidaVM.salidas.collectAsState()
    var aEvaluar by remember { mutableStateOf<SalidaPedagogica?>(null) }
    LaunchedEffect(usuario?.idUsuario) { usuario?.let { salidaVM.cargarDeProfesor(it.idUsuario) } }

    Pantalla(
        titulo = "Salidas pedagógicas",
        subtitulo = usuario?.nombreCompleto,
        viewModels = listOf(salidaVM, authVM),
        fab = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(AppScreens.NuevaSalida.route) },
                containerColor = Paleta.Azul, contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nueva salida", fontWeight = FontWeight.Bold) }
            )
        }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(salidas, key = { it.idSalida }) { s ->
                TarjetaSalida(s) {
                    if (s.estadoSalida == EstadosSalida.FINALIZADA && s.calificacion == null) {
                        Button(onClick = { aEvaluar = s }, colors = ButtonDefaults.buttonColors(containerColor = Paleta.Azul)) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Evaluar salida")
                        }
                    }
                    s.calificacion?.let { Text("Tu evaluación: ${"★".repeat(it)}", color = Paleta.Ambar, fontSize = 13.sp) }
                }
            }
            if (salidas.isEmpty()) item {
                MensajeVacio("Aún no planificas salidas. Toca \"Nueva salida\" para organizar una visita con tu curso.", Icons.Default.School)
            }
        }
    }

    aEvaluar?.let { s ->
        var nota by remember { mutableIntStateOf(5) }
        var comentario by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { aEvaluar = null },
            title = { Text("Evaluar salida") },
            text = {
                Column {
                    Text("${s.curso} · ${s.nombreRuta}", color = Paleta.TextoSuave, fontSize = 13.sp)
                    Row {
                        (1..5).forEach { i ->
                            IconButton(onClick = { nota = i }) {
                                Icon(if (i <= nota) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = "$i", tint = Paleta.Ambar)
                            }
                        }
                    }
                    CampoFormulario(comentario, { comentario = it }, "Comentario pedagógico", lineas = 3)
                }
            },
            confirmButton = { TextButton(onClick = { salidaVM.evaluar(s, nota, comentario); aEvaluar = null }) { Text("Enviar") } },
            dismissButton = { TextButton(onClick = { aEvaluar = null }) { Text("Cancelar") } }
        )
    }
}

/** Tarjeta reutilizada por el profesor, el guía y el encargado. */
@Composable
fun TarjetaSalida(s: SalidaPedagogica, acciones: @Composable ColumnScope.() -> Unit = {}) {
    Tarjeta {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("${s.colegio}", fontSize = 12.sp, color = Paleta.TextoSuave)
                Text("${s.curso} · ${s.cantidadAlumnos} estudiantes", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Paleta.Texto)
            }
            EtiquetaSalida(s.estadoSalida)
        }
        Spacer(Modifier.height(6.dp))
        FilaDato(Icons.Default.CalendarMonth, "${Formatos.fechaLarga(s.fechaProgramada)} · ${s.horaInicio} hrs")
        FilaDato(Icons.Default.Explore, "${s.nombreRuta} (${Formatos.duracion(s.tiempoRutaMin)})")
        if (s.nodosRuta.isNotEmpty()) Text(s.nodosRuta.joinToString(" → "), fontSize = 12.sp, color = Paleta.TextoSuave)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
            Icon(Icons.Default.Badge, contentDescription = null, tint = Paleta.Azul, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Guía: ${s.nombreGuia ?: "sin asignar"}", fontSize = 13.sp, color = Paleta.TextoMedio, modifier = Modifier.weight(1f))
            EtiquetaSalida(s.estadoAsignacion)
        }
        if (s.objetivos.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            s.objetivos.forEach { Text("• $it", fontSize = 12.sp, color = Paleta.TextoMedio) }
        }
        if (s.notasGuia.isNotBlank()) Text("Notas: ${s.notasGuia}", fontSize = 12.sp, color = Paleta.TextoSuave)
        if (s.comentarioGuia.isNotBlank()) Text("Comentario del guía: ${s.comentarioGuia}", fontSize = 12.sp, color = Paleta.Ambar)
        if (s.comentarioFinal.isNotBlank()) Text("Cierre del recorrido: ${s.comentarioFinal}", fontSize = 12.sp, color = Paleta.Verde)
        Spacer(Modifier.height(6.dp))
        acciones()
    }
}


// ---------- Vista previa para el panel Split de Android Studio (usa datos de prueba) ----------
@Preview(name = "Salidas del profesor", showBackground = true, showSystemUi = true)
@Composable
private fun SalidasScreenPreview() {
    val vm = rememberViewModelsPrueba(rol = Roles.PROFESOR) {
        usuario?.let { salida.cargarDeProfesor(it.idUsuario) }
    }
    VistaPrevia { SalidasScreen(rememberNavController(), vm.auth, vm.salida) }
}
