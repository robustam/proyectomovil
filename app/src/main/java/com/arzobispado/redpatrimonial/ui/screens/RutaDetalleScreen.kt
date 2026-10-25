package com.arzobispado.redpatrimonial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import com.arzobispado.redpatrimonial.data.model.NodoPatrimonial
import com.arzobispado.redpatrimonial.data.model.Roles
import com.arzobispado.redpatrimonial.navigation.AppScreens
import com.arzobispado.redpatrimonial.ui.components.*
import com.arzobispado.redpatrimonial.util.Formatos
import com.arzobispado.redpatrimonial.viewmodel.AuthViewModel
import com.arzobispado.redpatrimonial.viewmodel.NodoViewModel
import com.arzobispado.redpatrimonial.viewmodel.RutaViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.arzobispado.redpatrimonial.ui.preview.VistaPrevia
import com.arzobispado.redpatrimonial.ui.preview.rememberViewModelsPrueba

/** Detalle de una ruta: paradas, tiempos y recorrido paso a paso (sin GPS). */
@Composable
fun RutaDetalleScreen(
    navController: NavHostController,
    rutaId: Long,
    authVM: AuthViewModel,
    rutaVM: RutaViewModel,
    nodoVM: NodoViewModel
) {
    LaunchedEffect(rutaId) { rutaVM.cargarDetalle(rutaId) }
    val ruta by rutaVM.seleccionada.collectAsState()
    val actual by rutaVM.paradaActual.collectAsState()
    val nodos by nodoVM.nodos.collectAsState()
    val usuario by authVM.usuario.collectAsState()

    Pantalla(titulo = ruta?.nombreRuta ?: "Ruta", onVolver = { navController.popBackStack() }, viewModels = listOf(rutaVM)) {
        val r = ruta
        if (r == null || r.idRuta != rutaId) {
            MensajeVacio("Cargando ruta…")
        } else {
            // Nodos de la ruta para el mapa (si no están cargados se arman desde la parada)
            val nodosRuta = r.paradas.map { p ->
                nodos.firstOrNull { it.idNodo == p.idNodo }
                    ?: NodoPatrimonial(p.idNodo, p.nombreNodo, "", "", "TEMPLO", latitud = p.latitud, longitud = p.longitud)
            }
            val paradaEnCurso = r.paradas.getOrNull(actual)
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MapaEsquematico(
                    nodos = nodosRuta,
                    seleccionado = nodosRuta.getOrNull(actual.coerceAtLeast(0)),
                    onSeleccionar = { navController.navigate(AppScreens.Detail.createRoute(it.idNodo)) },
                    ruta = r.paradas.map { it.latitud to it.longitud },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
                Tarjeta {
                    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                        Dato("Distancia", "${r.distanciaKm} km")
                        Dato("Tiempo total", Formatos.duracion(r.tiempoEstimadoMin))
                        Dato("Dificultad", r.nivelDificultad)
                    }
                    if (r.descripcion.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(r.descripcion, fontSize = 13.sp, color = Paleta.TextoMedio)
                    }
                }

                if (paradaEnCurso != null) {
                    Surface(color = Paleta.AzulSuave, shape = MaterialTheme.shapes.large) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Recorrido en curso · parada ${actual + 1} de ${r.paradas.size}", fontSize = 12.sp, color = Paleta.AzulTexto)
                            Text(paradaEnCurso.nombreNodo, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Paleta.Texto)
                            Text("Tiempo sugerido: ${paradaEnCurso.tiempoSugeridoMin} min. ${paradaEnCurso.notasRecorrido}", fontSize = 13.sp, color = Paleta.TextoMedio)
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { rutaVM.siguienteParada() }, colors = ButtonDefaults.buttonColors(containerColor = Paleta.Azul)) {
                                    Text(if (actual == r.paradas.lastIndex) "Finalizar recorrido" else "Siguiente parada")
                                }
                                OutlinedButton(onClick = { rutaVM.terminarRecorrido() }) { Text("Salir") }
                            }
                        }
                    }
                }

                TituloSeccion("Paradas")
                r.paradas.forEachIndexed { i, p ->
                    val estado = when {
                        actual < 0 -> 0
                        i < actual -> 1   // visitada
                        i == actual -> 2  // actual
                        else -> 0
                    }
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            Modifier
                                .size(30.dp)
                                .background(
                                    when (estado) { 1 -> Paleta.Verde; 2 -> Paleta.Azul; else -> Paleta.TextoSuave },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (estado == 1) Icon(Icons.Default.Check, contentDescription = "Visitada", tint = Color.White, modifier = Modifier.size(16.dp))
                            else Text("${i + 1}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p.nombreNodo, fontWeight = FontWeight.Bold, color = Paleta.Texto)
                            Text("${p.tiempoSugeridoMin} min de visita${if (p.notasRecorrido.isNotBlank()) " · ${p.notasRecorrido}" else ""}",
                                fontSize = 12.sp, color = Paleta.TextoSuave)
                        }
                        TextButton(onClick = { navController.navigate(AppScreens.Detail.createRoute(p.idNodo)) }) { Text("Ficha", fontSize = 12.sp) }
                    }
                }

                if (actual < 0) {
                    BotonPrincipal("Iniciar recorrido", onClick = { rutaVM.iniciarRecorrido() }, icono = Icons.Default.PlayArrow)
                    Text("El recorrido avanza parada por parada. La navegación con GPS se agregará cuando se implementen los recursos del teléfono.",
                        fontSize = 11.sp, color = Paleta.TextoSuave)
                }
                if (usuario?.rol == Roles.PROFESOR) {
                    OutlinedButton(onClick = { navController.navigate(AppScreens.NuevaSalida.route) }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.School, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Planificar salida pedagógica con esta ruta")
                    }
                }
            }
        }
    }
}

@Composable
private fun Dato(titulo: String, valor: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Paleta.Texto)
        Text(titulo, fontSize = 11.sp, color = Paleta.TextoSuave)
    }
}


// ---------- Vista previa para el panel Split de Android Studio (usa datos de prueba) ----------
@Preview(name = "Detalle de ruta", showBackground = true, showSystemUi = true)
@Composable
private fun RutaDetalleScreenPreview() {
    val vm = rememberViewModelsPrueba(rol = Roles.TURISTA) {
        ruta.cargarDetalle(1)
    }
    VistaPrevia { RutaDetalleScreen(rememberNavController(), 1, vm.auth, vm.ruta, vm.nodo) }
}
