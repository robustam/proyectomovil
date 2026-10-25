package com.arzobispado.redpatrimonial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.arzobispado.redpatrimonial.data.model.CategoriasAviso
import com.arzobispado.redpatrimonial.data.model.EstadosApertura
import com.arzobispado.redpatrimonial.navigation.AppScreens
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

/** CU01 + CU04: mapa interactivo de la red con el estado de apertura en tiempo real. */
@Composable
fun MapScreen(
    navController: NavHostController,
    authVM: AuthViewModel,
    nodoVM: NodoViewModel,
    eventoVM: EventoViewModel,
    avisoVM: AvisoViewModel
) {
    val usuario by authVM.usuario.collectAsState()
    val nodos by nodoVM.nodos.collectAsState()
    val avisos by avisoVM.avisos.collectAsState()
    val eventos by eventoVM.eventos.collectAsState()
    var seleccionadoId by rememberSaveable { mutableStateOf<Long?>(null) }
    val seleccionado = nodos.firstOrNull { it.idNodo == seleccionadoId } ?: nodos.firstOrNull()

    Pantalla(
        titulo = "Red Patrimonial",
        subtitulo = usuario?.let { "Hola, ${it.nombreCompleto.substringBefore(' ')}" } ?: "Modo invitado",
        viewModels = listOf(authVM, nodoVM, avisoVM),
        acciones = {
            IconButton(onClick = { nodoVM.cargar(); avisoVM.cargar(); eventoVM.cargar() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Color.White)
            }
            if (usuario == null) {
                TextButton(onClick = { navController.navigate(AppScreens.Login.route) }) {
                    Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Ingresar", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Buscador (lleva a la pantalla de búsqueda con filtros)
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(AppScreens.Explorar.route) }
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Paleta.TextoSuave)
                        Spacer(Modifier.width(8.dp))
                        Text("Buscar templo, museo o comuna…", color = Paleta.TextoSuave)
                    }
                }
            }

            // Accesos rápidos
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { Acceso("Agenda cultural (${eventos.size})", Icons.Default.Event) { navController.navigate(AppScreens.EventList.route) } }
                    item { Acceso("Rutas patrimoniales", Icons.Default.Explore) { navController.navigate(AppScreens.RoutePlanner.route) } }
                    item { Acceso("Avisos en vivo (${avisos.size})", Icons.Default.Notifications) { navController.navigate(AppScreens.CommunityAlerts.route) } }
                }
            }

            // Último aviso de la red
            avisos.firstOrNull()?.let { aviso ->
                item {
                    Surface(
                        color = Paleta.AmbarSuave,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(AppScreens.CommunityAlerts.route) }
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(CategoriasAviso.icono(aviso.categoria), fontSize = 20.sp)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text("${aviso.nombreNodo}: ${aviso.titulo}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Paleta.Ambar, maxLines = 1)
                                Text(Formatos.hace(aviso.haceMinutos), fontSize = 11.sp, color = Paleta.Ambar)
                            }
                        }
                    }
                }
            }

            // Mapa
            item {
                MapaEsquematico(
                    nodos = nodos,
                    seleccionado = seleccionado,
                    onSeleccionar = { seleccionadoId = it.idNodo },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            }

            // Leyenda
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    EstadosApertura.TODOS.forEach { e ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(10.dp).background(colorEstado(e), CircleShape))
                            Spacer(Modifier.width(4.dp))
                            Text(EstadosApertura.nombreVisible(e), fontSize = 11.sp, color = Paleta.TextoMedio)
                        }
                    }
                }
            }

            // Tarjeta del templo seleccionado
            seleccionado?.let { nodo ->
                item {
                    Tarjeta {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(nodo.nombreOficial, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Paleta.Texto)
                                Text("${nodo.direccion}, ${nodo.comuna}", fontSize = 12.sp, color = Paleta.TextoSuave)
                            }
                            EtiquetaEstado(nodo.estadoActual)
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { navController.navigate(AppScreens.Detail.createRoute(nodo.idNodo)) },
                                colors = ButtonDefaults.buttonColors(containerColor = Paleta.Azul),
                                modifier = Modifier.weight(1f)
                            ) { Text("Ver ficha") }
                            OutlinedButton(
                                onClick = { navController.navigate(AppScreens.EventList.route) },
                                modifier = Modifier.weight(1f)
                            ) { Text("Ver agenda") }
                        }
                    }
                }
            }

            item { TituloSeccion("Lugares de la red (${nodos.size})", Modifier.padding(top = 4.dp)) }
            items(nodos, key = { it.idNodo }) { nodo ->
                TarjetaNodo(nodo = nodo, onClick = { navController.navigate(AppScreens.Detail.createRoute(nodo.idNodo)) })
            }
            if (nodos.isEmpty()) item { MensajeVacio("Cargando la red patrimonial…") }
        }
    }
}

@Composable
private fun Acceso(texto: String, icono: ImageVector, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(texto, fontSize = 12.sp) },
        leadingIcon = { Icon(icono, contentDescription = null, modifier = Modifier.size(18.dp), tint = Paleta.Azul) },
        colors = AssistChipDefaults.assistChipColors(containerColor = Color.White)
    )
}


// ---------- Vista previa para el panel Split de Android Studio (usa datos de prueba) ----------
@Preview(name = "Mapa (invitado)", showBackground = true, showSystemUi = true)
@Composable
private fun MapScreenPreview() {
    val vm = rememberViewModelsPrueba()
    VistaPrevia { MapScreen(rememberNavController(), vm.auth, vm.nodo, vm.evento, vm.aviso) }
}
