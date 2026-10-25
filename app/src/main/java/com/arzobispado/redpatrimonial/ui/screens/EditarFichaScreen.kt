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
import com.arzobispado.redpatrimonial.ui.components.*
import com.arzobispado.redpatrimonial.viewmodel.AuthViewModel
import com.arzobispado.redpatrimonial.viewmodel.NodoViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.arzobispado.redpatrimonial.ui.preview.VistaPrevia
import com.arzobispado.redpatrimonial.ui.preview.rememberViewModelsPrueba
import com.arzobispado.redpatrimonial.data.model.Roles

/**
 * CU13: modificar ficha y galería multimedia.
 * Los recursos se registran con un título y una URL de prueba; la cámara
 * se integrará cuando se vean los recursos del teléfono en clases.
 */
@Composable
fun EditarFichaScreen(navController: NavHostController, authVM: AuthViewModel, nodoVM: NodoViewModel) {
    val usuario by authVM.usuario.collectAsState()
    val nodos by nodoVM.nodos.collectAsState()
    val nodo = nodos.firstOrNull { it.idNodo == usuario?.idNodoAsignado }

    var nombre by remember(nodo?.idNodo) { mutableStateOf(nodo?.nombreOficial ?: "") }
    var direccion by remember(nodo?.idNodo) { mutableStateOf(nodo?.direccion ?: "") }
    var resena by remember(nodo?.idNodo) { mutableStateOf(nodo?.resenaHistorica ?: "") }
    var estilo by remember(nodo?.idNodo) { mutableStateOf(nodo?.estiloArquitectonico ?: "") }
    var anio by remember(nodo?.idNodo) { mutableStateOf(nodo?.anioConstruccion?.toString() ?: "") }
    var tipoRecurso by remember { mutableStateOf("IMAGEN") }
    var tituloRecurso by remember { mutableStateOf("") }
    var urlRecurso by remember { mutableStateOf("") }

    Pantalla(titulo = "Editar ficha", subtitulo = nodo?.nombreOficial, onVolver = { navController.popBackStack() }, viewModels = listOf(nodoVM)) {
        if (nodo == null) {
            MensajeVacio("No tienes un templo asignado")
        } else {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TituloSeccion("Datos de la ficha")
                CampoFormulario(nombre, { nombre = it }, "Nombre oficial", icono = Icons.Default.Church)
                CampoFormulario(direccion, { direccion = it }, "Dirección", icono = Icons.Default.LocationOn)
                CampoFormulario(estilo, { estilo = it }, "Estilo arquitectónico")
                CampoFormulario(anio, { anio = it.filter(Char::isDigit).take(4) }, "Año de construcción", teclado = KeyboardType.Number)
                CampoFormulario(resena, { resena = it }, "Reseña histórica", lineas = 5)
                BotonPrincipal("Guardar cambios", icono = Icons.Default.Save, onClick = {
                    if (nombre.isBlank() || direccion.isBlank()) {
                        nodoVM.actualizarFicha(nodo, nodo.nombreOficial, nodo.direccion, resena, estilo, anio.toIntOrNull()) {}
                    } else {
                        nodoVM.actualizarFicha(nodo, nombre.trim(), direccion.trim(), resena.trim(), estilo.trim(), anio.toIntOrNull()) {}
                    }
                })

                TituloSeccion("Galería multimedia (${nodo.recursos.size})", Modifier.padding(top = 8.dp))
                nodo.recursos.forEach { r ->
                    Tarjeta {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RecursoSimulado("", r.tipoRecurso, Modifier.size(56.dp))
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(r.titulo, fontWeight = FontWeight.Bold)
                                Text("${r.tipoRecurso}${if (r.esPrincipal) " · principal" else ""}", fontSize = 12.sp, color = Paleta.TextoSuave)
                            }
                            IconButton(onClick = { nodoVM.eliminarRecurso(nodo.idNodo, r.idRecurso) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Paleta.TextoSuave)
                            }
                        }
                    }
                }
                Tarjeta {
                    Text("Agregar recurso", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    SelectorOpciones(listOf("IMAGEN", "AUDIO", "VIDEO"), tipoRecurso, { it.lowercase().replaceFirstChar { c -> c.uppercase() } }, { tipoRecurso = it })
                    Spacer(Modifier.height(6.dp))
                    CampoFormulario(tituloRecurso, { tituloRecurso = it }, "Título (ej: Altar mayor)")
                    CampoFormulario(urlRecurso, { urlRecurso = it }, "URL del recurso (opcional)", ayuda = "Se usa una URL de prueba si la dejas vacía")
                    Spacer(Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = {
                            if (tituloRecurso.isNotBlank()) {
                                nodoVM.agregarRecurso(nodo.idNodo, tipoRecurso, tituloRecurso.trim(), urlRecurso.trim())
                                tituloRecurso = ""
                                urlRecurso = ""
                            }
                        },
                        enabled = tituloRecurso.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Agregar a la galería")
                    }
                }
            }
        }
    }
}


// ---------- Vista previa para el panel Split de Android Studio (usa datos de prueba) ----------
@Preview(name = "Editar ficha", showBackground = true, showSystemUi = true)
@Composable
private fun EditarFichaScreenPreview() {
    val vm = rememberViewModelsPrueba(rol = Roles.ENCARGADO)
    VistaPrevia { EditarFichaScreen(rememberNavController(), vm.auth, vm.nodo) }
}
