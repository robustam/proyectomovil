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

/**
 * CU06 / CU09: trazar una ruta personalizada (o ruta de aprendizaje para profesores)
 * con cálculo dinámico de distancia y tiempo estimado.
 */
@Composable
fun CrearRutaScreen(navController: NavHostController, authVM: AuthViewModel, rutaVM: RutaViewModel, nodoVM: NodoViewModel) {
    val usuario by authVM.usuario.collectAsState()
    val borrador by rutaVM.borrador.collectAsState()
    val calculo by rutaVM.calculo.collectAsState()
    val nodos by nodoVM.nodos.collectAsState()
    val esProfesor = usuario?.rol == Roles.PROFESOR
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var enfoque by remember { mutableStateOf(if (esProfesor) "Ruta de aprendizaje" else "Personalizada") }
    val enfoques = listOf("Personalizada", "Ruta de aprendizaje", "Histórico", "Comunitario")

    Pantalla(
        titulo = if (esProfesor) "Nueva ruta de aprendizaje" else "Crear mi ruta",
        onVolver = { navController.popBackStack() },
        viewModels = listOf(rutaVM)
    ) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                CampoFormulario(nombre, { nombre = it }, "Nombre de la ruta", icono = Icons.Default.Flag)
            }
            item {
                CampoFormulario(descripcion, { descripcion = it }, "Descripción (opcional)", lineas = 2)
            }
            item {
                SelectorOpciones(enfoques, enfoque, { it }, { enfoque = it })
            }

            // Resumen calculado en vivo
            item {
                Tarjeta {
                    Text("Resumen de tu ruta", fontWeight = FontWeight.Bold, color = Paleta.Texto)
                    Spacer(Modifier.height(6.dp))
                    Text("📍 ${borrador.size} lugares · 📏 ${calculo.distanciaKm} km · ⏱️ ${Formatos.duracion(calculo.minutosTotal)}",
                        fontSize = 14.sp, color = Paleta.TextoMedio)
                    Text("Traslados a pie: ${Formatos.duracion(calculo.minutosTraslado)} · Dificultad: ${calculo.dificultad}",
                        fontSize = 12.sp, color = Paleta.TextoSuave)
                }
            }

            if (borrador.isNotEmpty()) item { TituloSeccion("Orden del recorrido") }
            items(borrador.size) { i ->
                val p = borrador[i]
                Tarjeta {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${i + 1}.", fontWeight = FontWeight.Bold, color = Paleta.Azul)
                        Spacer(Modifier.width(8.dp))
                        Text(p.nombreNodo, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), fontSize = 14.sp)
                        IconButton(onClick = { rutaVM.moverArriba(i) }, enabled = i > 0) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Subir")
                        }
                        IconButton(onClick = { nodoVM.nodoPorId(p.idNodo)?.let { rutaVM.alternarParada(it) } }) {
                            Icon(Icons.Default.Close, contentDescription = "Quitar")
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Tiempo de visita:", fontSize = 12.sp, color = Paleta.TextoSuave)
                        IconButton(onClick = { rutaVM.cambiarTiempo(p.idNodo, p.tiempoSugeridoMin - 5) }) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Menos tiempo")
                        }
                        Text("${p.tiempoSugeridoMin} min", fontWeight = FontWeight.Bold)
                        IconButton(onClick = { rutaVM.cambiarTiempo(p.idNodo, p.tiempoSugeridoMin + 5) }) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = "Más tiempo")
                        }
                    }
                }
            }

            item {
                BotonPrincipal(
                    texto = "Guardar ruta",
                    icono = Icons.Default.Save,
                    onClick = {
                        rutaVM.guardarRuta(nombre, descripcion, enfoque, usuario) { ruta ->
                            navController.navigate(AppScreens.RutaDetalle.createRoute(ruta.idRuta)) {
                                popUpTo(AppScreens.CrearRuta.route) { inclusive = true }
                            }
                        }
                    }
                )
                if (usuario == null) Text("Puedes armar la ruta como invitado, pero debes iniciar sesión para guardarla.",
                    fontSize = 11.sp, color = Paleta.TextoSuave)
            }

            item { TituloSeccion("Agregar lugares", Modifier.padding(top = 8.dp)) }
            items(nodos, key = { it.idNodo }) { nodo ->
                val elegido = borrador.any { it.idNodo == nodo.idNodo }
                TarjetaNodo(
                    nodo = nodo,
                    onClick = { rutaVM.alternarParada(nodo) },
                    extremoDerecho = { Checkbox(checked = elegido, onCheckedChange = { rutaVM.alternarParada(nodo) }) }
                )
            }
        }
    }
}


// ---------- Vista previa para el panel Split de Android Studio (usa datos de prueba) ----------
@Preview(name = "Crear ruta", showBackground = true, showSystemUi = true)
@Composable
private fun CrearRutaScreenPreview() {
    val vm = rememberViewModelsPrueba(rol = Roles.TURISTA) {
        nodo.nodoPorId(1)?.let { ruta.alternarParada(it) }
        nodo.nodoPorId(4)?.let { ruta.alternarParada(it) }
    }
    VistaPrevia { CrearRutaScreen(rememberNavController(), vm.auth, vm.ruta, vm.nodo) }
}
