package com.arzobispado.redpatrimonial.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.arzobispado.redpatrimonial.navigation.AppScreens
import com.arzobispado.redpatrimonial.ui.components.*
import com.arzobispado.redpatrimonial.util.Formatos
import com.arzobispado.redpatrimonial.viewmodel.AuthViewModel
import com.arzobispado.redpatrimonial.viewmodel.RutaViewModel
import com.arzobispado.redpatrimonial.viewmodel.SalidaViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.arzobispado.redpatrimonial.ui.preview.VistaPrevia
import com.arzobispado.redpatrimonial.ui.preview.rememberViewModelsPrueba
import com.arzobispado.redpatrimonial.data.model.Roles

/**
 * CU07 + CU08: asistente de 3 pasos para planificar una salida pedagógica.
 * Paso 1: curso y fecha · Paso 2: ruta y objetivos · Paso 3: guía y envío.
 */
@Composable
fun NuevaSalidaScreen(navController: NavHostController, authVM: AuthViewModel, salidaVM: SalidaViewModel, rutaVM: RutaViewModel) {
    val usuario by authVM.usuario.collectAsState()
    val paso by salidaVM.paso.collectAsState()
    val f by salidaVM.formulario.collectAsState()
    val objetivos by salidaVM.objetivos.collectAsState()
    val guias by salidaVM.guias.collectAsState()
    val rutas by rutaVM.rutas.collectAsState()
    var iniciado by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!iniciado) {
            salidaVM.iniciarFormulario()
            rutaVM.cargar(usuario?.idUsuario)
            iniciado = true
        }
    }

    val titulos = listOf("Curso y fecha", "Ruta y objetivos", "Guía y confirmación")

    Pantalla(
        titulo = "Nueva salida pedagógica",
        subtitulo = "Paso $paso de 3: ${titulos[paso - 1]}",
        onVolver = { if (paso > 1) salidaVM.pasoAnterior() else navController.popBackStack() },
        viewModels = listOf(salidaVM)
    ) {
        LinearProgressIndicator(progress = { paso / 3f }, modifier = Modifier.fillMaxWidth(), color = Paleta.Azul)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (paso) {
                1 -> {
                    TituloSeccion("Institución y curso")
                    CampoFormulario(f.colegio, { salidaVM.actualizar(f.copy(colegio = it)) }, "Colegio o institución", icono = Icons.Default.School)
                    CampoFormulario(f.curso, { salidaVM.actualizar(f.copy(curso = it)) }, "Curso (ej: 8° Básico A)", icono = Icons.Default.Groups)
                    CampoFormulario(f.cantidadAlumnos, { salidaVM.actualizar(f.copy(cantidadAlumnos = it.filter(Char::isDigit))) },
                        "Cantidad de estudiantes", icono = Icons.Default.Person, teclado = KeyboardType.Number)
                    TituloSeccion("Fecha y hora de la visita")
                    CampoFormulario(f.fecha, { salidaVM.actualizar(f.copy(fecha = it)) }, "Fecha", icono = Icons.Default.CalendarMonth,
                        ayuda = "Formato AAAA-MM-DD · ${if (Formatos.esFechaValida(f.fecha)) Formatos.fechaLarga(f.fecha) else "fecha no válida"}")
                    CampoFormulario(f.hora, { salidaVM.actualizar(f.copy(hora = it)) }, "Hora de inicio", icono = Icons.Default.Schedule, ayuda = "Formato HH:MM")
                }
                2 -> {
                    TituloSeccion("Ruta seleccionada")
                    rutas.forEach { r ->
                        val elegida = f.idRuta == r.idRuta
                        OutlinedCard(
                            onClick = { salidaVM.actualizar(f.copy(idRuta = r.idRuta)) },
                            border = BorderStroke(if (elegida) 2.dp else 1.dp, if (elegida) Paleta.Azul else Paleta.Borde),
                            colors = CardDefaults.outlinedCardColors(containerColor = if (elegida) Paleta.AzulMuySuave else Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = elegida, onClick = { salidaVM.actualizar(f.copy(idRuta = r.idRuta)) })
                                Column(Modifier.weight(1f)) {
                                    Text(r.nombreRuta, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${r.paradas.size} templos · ${Formatos.duracion(r.tiempoEstimadoMin)} · ${r.enfoque}", fontSize = 12.sp, color = Paleta.TextoSuave)
                                }
                            }
                        }
                    }
                    TextButton(onClick = { navController.navigate(AppScreens.CrearRuta.route) }) {
                        Icon(Icons.Default.AddLocationAlt, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Crear una ruta de aprendizaje propia")
                    }
                    TituloSeccion("Objetivos pedagógicos")
                    objetivos.forEach { o ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = o.idObjetivo in f.objetivos, onCheckedChange = { salidaVM.alternarObjetivo(o.idObjetivo) })
                            Column {
                                Text("${o.codigoCurricular} - ${o.descripcion}", fontSize = 14.sp)
                                Text(o.asignatura, fontSize = 11.sp, color = Paleta.TextoSuave)
                            }
                        }
                    }
                    Text("Códigos curriculares de ejemplo para la simulación.", fontSize = 11.sp, color = Paleta.TextoSuave)
                }
                else -> {
                    val ruta = rutas.firstOrNull { it.idRuta == f.idRuta }
                    Tarjeta {
                        Text("Resumen de la visita", fontWeight = FontWeight.Bold)
                        FilaDato(Icons.Default.CalendarMonth, "${Formatos.fechaLarga(f.fecha)} · ${f.hora} hrs")
                        FilaDato(Icons.Default.Groups, "${f.colegio} · ${f.curso} (${f.cantidadAlumnos} estudiantes)")
                        FilaDato(Icons.Default.Explore, "${ruta?.nombreRuta ?: "-"} · ${Formatos.duracion(ruta?.tiempoEstimadoMin ?: 0)}")
                    }
                    TituloSeccion("Guía sugerido por la red")
                    guias.forEach { g ->
                        val elegido = f.idGuia == g.idGuia
                        OutlinedCard(
                            onClick = { salidaVM.actualizar(f.copy(idGuia = g.idGuia)) },
                            border = BorderStroke(if (elegido) 2.dp else 1.dp, if (elegido) Paleta.Azul else Paleta.Borde),
                            colors = CardDefaults.outlinedCardColors(containerColor = if (elegido) Paleta.AzulMuySuave else Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = elegido, onClick = { salidaVM.actualizar(f.copy(idGuia = g.idGuia)) })
                                Column(Modifier.weight(1f)) {
                                    Text(g.nombre, fontWeight = FontWeight.Bold)
                                    Text("${g.especialidad} · ${g.calificacion} ★ · ${g.idiomas}", fontSize = 12.sp, color = Paleta.TextoSuave)
                                }
                                if (g.disponibleEnFecha) Etiqueta("Disponible", Paleta.VerdeSuave, Paleta.Verde)
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = f.idGuia == null, onClick = { salidaVM.actualizar(f.copy(idGuia = null)) })
                        Text("Sin guía (visita autónoma)")
                    }
                    CampoFormulario(f.notasGuia, { salidaVM.actualizar(f.copy(notasGuia = it)) },
                        "Notas especiales para el guía", ayuda = "Ej: requerimientos de accesibilidad", lineas = 3)
                }
            }
        }

        // Botones de navegación del asistente
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (paso > 1) {
                OutlinedButton(onClick = { salidaVM.pasoAnterior() }, modifier = Modifier.weight(1f).height(50.dp)) { Text("Atrás") }
            }
            if (paso < 3) {
                BotonPrincipal(
                    texto = if (paso == 1) "Siguiente: ruta" else "Siguiente: asignar guía",
                    onClick = { salidaVM.siguientePaso() },
                    modifier = Modifier.weight(1f)
                )
            } else {
                BotonPrincipal(
                    texto = "Enviar solicitud",
                    icono = Icons.AutoMirrored.Filled.Send,
                    onClick = { usuario?.let { u -> salidaVM.enviar(u) { navController.popBackStack() } } },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}


// ---------- Vista previa para el panel Split de Android Studio (usa datos de prueba) ----------
@Preview(name = "Nueva salida (paso 1)", showBackground = true, showSystemUi = true)
@Composable
private fun NuevaSalidaScreenPreview() {
    val vm = rememberViewModelsPrueba(rol = Roles.PROFESOR) {
        salida.iniciarFormulario(); ruta.cargar(usuario?.idUsuario)
    }
    VistaPrevia { NuevaSalidaScreen(rememberNavController(), vm.auth, vm.salida, vm.ruta) }
}
