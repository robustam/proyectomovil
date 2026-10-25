package com.arzobispado.redpatrimonial.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.arzobispado.redpatrimonial.navigation.AppScreens
import com.arzobispado.redpatrimonial.ui.components.*
import com.arzobispado.redpatrimonial.util.Formatos
import com.arzobispado.redpatrimonial.viewmodel.NodoViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.arzobispado.redpatrimonial.ui.preview.VistaPrevia
import com.arzobispado.redpatrimonial.ui.preview.rememberViewModelsPrueba

/** CU02: buscar templos por texto, tipo, comuna y "abiertos ahora". */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorarScreen(navController: NavHostController, nodoVM: NodoViewModel) {
    val filtros by nodoVM.filtros.collectAsState()
    val resultados by nodoVM.nodosFiltrados.collectAsState()
    val comunas by nodoVM.comunas.collectAsState()
    val favoritos by nodoVM.favoritos.collectAsState()
    var menuComuna by remember { mutableStateOf(false) }
    val tipos = listOf("TEMPLO", "MUSEO", "COLEGIO", "ARCHIVO")

    Pantalla(
        titulo = "Explorar patrimonio",
        onVolver = if (navController.previousBackStackEntry != null) ({ navController.popBackStack() }) else null,
        viewModels = listOf(nodoVM)
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            OutlinedTextField(
                value = filtros.texto,
                onValueChange = { nodoVM.actualizarFiltros(filtros.copy(texto = it)) },
                placeholder = { Text("Nombre, comuna o estilo…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (filtros.texto.isNotEmpty()) {
                        IconButton(onClick = { nodoVM.actualizarFiltros(filtros.copy(texto = "")) }) {
                            Icon(Icons.Default.Close, contentDescription = "Borrar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))

            // Filtro por tipo de nodo
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(selected = filtros.tipo == null, onClick = { nodoVM.actualizarFiltros(filtros.copy(tipo = null)) },
                        label = { Text("Todos") })
                }
                items(tipos) { t ->
                    FilterChip(
                        selected = filtros.tipo == t,
                        onClick = { nodoVM.actualizarFiltros(filtros.copy(tipo = if (filtros.tipo == t) null else t)) },
                        label = { Text(Formatos.tipoNodo(t)) },
                        leadingIcon = { Icon(iconoTipo(t), contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }

            // Comuna y "abiertos ahora"
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box {
                    AssistChip(
                        onClick = { menuComuna = true },
                        label = { Text(filtros.comuna ?: "Todas las comunas") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                    )
                    DropdownMenu(expanded = menuComuna, onDismissRequest = { menuComuna = false }) {
                        DropdownMenuItem(text = { Text("Todas las comunas") }, onClick = {
                            nodoVM.actualizarFiltros(filtros.copy(comuna = null)); menuComuna = false
                        })
                        comunas.forEach { c ->
                            DropdownMenuItem(text = { Text(c) }, onClick = {
                                nodoVM.actualizarFiltros(filtros.copy(comuna = c)); menuComuna = false
                            })
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                Text("Abiertos ahora", fontSize = 13.sp, color = Paleta.TextoMedio)
                Spacer(Modifier.width(6.dp))
                Switch(checked = filtros.soloAbiertos, onCheckedChange = { nodoVM.actualizarFiltros(filtros.copy(soloAbiertos = it)) })
            }
            Text("${resultados.size} resultado(s)", fontSize = 12.sp, color = Paleta.TextoSuave)
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(resultados, key = { it.idNodo }) { nodo ->
                TarjetaNodo(
                    nodo = nodo,
                    onClick = { navController.navigate(AppScreens.Detail.createRoute(nodo.idNodo)) },
                    extremoDerecho = {
                        if (nodo.idNodo in favoritos) Icon(Icons.Default.Favorite, contentDescription = "Favorito", tint = Paleta.Rojo)
                    }
                )
            }
            if (resultados.isEmpty()) item { MensajeVacio("No hay lugares que coincidan con los filtros", Icons.Default.SearchOff) }
        }
    }
}


// ---------- Vista previa para el panel Split de Android Studio (usa datos de prueba) ----------
@Preview(name = "Explorar", showBackground = true, showSystemUi = true)
@Composable
private fun ExplorarScreenPreview() {
    val vm = rememberViewModelsPrueba()
    VistaPrevia { ExplorarScreen(rememberNavController(), vm.nodo) }
}
