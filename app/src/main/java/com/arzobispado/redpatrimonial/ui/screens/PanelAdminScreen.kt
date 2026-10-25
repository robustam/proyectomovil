package com.arzobispado.redpatrimonial.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.arzobispado.redpatrimonial.data.model.EstadosApertura
import com.arzobispado.redpatrimonial.data.model.Roles
import com.arzobispado.redpatrimonial.ui.components.*
import com.arzobispado.redpatrimonial.util.Formatos
import com.arzobispado.redpatrimonial.viewmodel.AuthViewModel
import com.arzobispado.redpatrimonial.viewmodel.AvisoViewModel
import com.arzobispado.redpatrimonial.viewmodel.EventoViewModel
import com.arzobispado.redpatrimonial.viewmodel.NodoViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.arzobispado.redpatrimonial.ui.preview.VistaPrevia
import com.arzobispado.redpatrimonial.ui.preview.rememberViewModelsPrueba

/** CU20 (permisos por nodo) y CU21 (indicadores de la red) para el administrador. */
@Composable
fun PanelAdminScreen(
    navController: NavHostController,
    authVM: AuthViewModel,
    nodoVM: NodoViewModel,
    eventoVM: EventoViewModel,
    avisoVM: AvisoViewModel
) {
    val nodos by nodoVM.nodos.collectAsState()
    val eventos by eventoVM.eventos.collectAsState()
    val avisos by avisoVM.avisos.collectAsState()
    val usuarios by authVM.usuarios.collectAsState()
    LaunchedEffect(Unit) { authVM.cargarUsuarios() }

    Pantalla(titulo = "Panel de administración", subtitulo = "Indicadores de la red", viewModels = listOf(authVM, nodoVM)) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Indicador("${nodos.size}", "Nodos en la red", Modifier.weight(1f))
                    Indicador("${eventos.size}", "Eventos próximos", Modifier.weight(1f))
                    Indicador("${avisos.size}", "Avisos activos", Modifier.weight(1f))
                }
            }
            item {
                Tarjeta {
                    Text("Estado de apertura ahora", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    EstadosApertura.TODOS.forEach { e ->
                        val n = nodos.count { it.estadoActual == e }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(EstadosApertura.nombreVisible(e), Modifier.width(110.dp), fontSize = 13.sp)
                            LinearProgressIndicator(
                                progress = { if (nodos.isEmpty()) 0f else n / nodos.size.toFloat() },
                                color = colorEstado(e),
                                modifier = Modifier.weight(1f).height(8.dp)
                            )
                            Text("  $n", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            item {
                Tarjeta {
                    Text("Nodos por comuna", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    nodos.groupingBy { it.comuna }.eachCount().toList().sortedByDescending { it.second }.forEach { (comuna, n) ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Text(comuna, Modifier.weight(1f), fontSize = 13.sp)
                            Text("$n", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    Text("Ayuda a medir si la red llega más allá del centro de Santiago.", fontSize = 11.sp, color = Paleta.TextoSuave)
                }
            }
            item { TituloSeccion("Usuarios y permisos por nodo") }
            items(usuarios, key = { it.idUsuario }) { u ->
                var menu by remember { mutableStateOf(false) }
                Tarjeta {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(u.nombreCompleto, fontWeight = FontWeight.Bold)
                            Text("${u.email} · ${Roles.nombreVisible(u.rol)}", fontSize = 12.sp, color = Paleta.TextoSuave)
                            if (u.rol == Roles.ENCARGADO) {
                                Text("Templo: ${nodos.firstOrNull { it.idNodo == u.idNodoAsignado }?.nombreOficial ?: "sin asignar"}",
                                    fontSize = 12.sp, color = Paleta.Azul)
                            }
                        }
                        Switch(checked = u.estado == "ACTIVO", onCheckedChange = { authVM.cambiarEstadoUsuario(u) })
                    }
                    if (u.rol == Roles.ENCARGADO) {
                        Box {
                            TextButton(onClick = { menu = true }) {
                                Text("Asignar templo")
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                nodos.forEach { n ->
                                    DropdownMenuItem(
                                        text = { Text("${n.nombreOficial} (${Formatos.tipoNodo(n.tipoNodo)})") },
                                        onClick = { authVM.asignarNodo(u.idUsuario, n.idNodo); menu = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ---------- Vista previa para el panel Split de Android Studio (usa datos de prueba) ----------
@Preview(name = "Panel de administración", showBackground = true, showSystemUi = true)
@Composable
private fun PanelAdminScreenPreview() {
    val vm = rememberViewModelsPrueba(rol = Roles.ADMIN) {
        auth.cargarUsuarios()
    }
    VistaPrevia { PanelAdminScreen(rememberNavController(), vm.auth, vm.nodo, vm.evento, vm.aviso) }
}
