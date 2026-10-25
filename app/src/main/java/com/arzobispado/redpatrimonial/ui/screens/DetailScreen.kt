package com.arzobispado.redpatrimonial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.arzobispado.redpatrimonial.data.model.EstadosApertura
import com.arzobispado.redpatrimonial.data.model.Roles
import com.arzobispado.redpatrimonial.navigation.AppScreens
import com.arzobispado.redpatrimonial.ui.components.*
import com.arzobispado.redpatrimonial.util.Formatos
import com.arzobispado.redpatrimonial.viewmodel.AuthViewModel
import com.arzobispado.redpatrimonial.viewmodel.EventoViewModel
import com.arzobispado.redpatrimonial.viewmodel.NodoViewModel
import com.arzobispado.redpatrimonial.viewmodel.RutaViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.arzobispado.redpatrimonial.ui.preview.VistaPrevia
import com.arzobispado.redpatrimonial.ui.preview.rememberViewModelsPrueba

/** CU03: ficha patrimonial con pestañas Historia / Horarios / Galería / Eventos. */
@Composable
fun DetailScreen(
    navController: NavHostController,
    nodoId: Long,
    authVM: AuthViewModel,
    nodoVM: NodoViewModel,
    eventoVM: EventoViewModel,
    rutaVM: RutaViewModel
) {
    LaunchedEffect(nodoId) {
        nodoVM.cargarDetalle(nodoId)
        eventoVM.cargarDeNodo(nodoId)
    }
    val nodo by nodoVM.seleccionado.collectAsState()
    val eventos by eventoVM.eventosNodo.collectAsState()
    val favoritos by nodoVM.favoritos.collectAsState()
    val borrador by rutaVM.borrador.collectAsState()
    val usuario by authVM.usuario.collectAsState()
    var pestana by remember { mutableIntStateOf(0) }
    var dialogoInvitado by remember { mutableStateOf<String?>(null) }

    Pantalla(titulo = "Ficha patrimonial", onVolver = { navController.popBackStack() }, viewModels = listOf(nodoVM, eventoVM)) {
        val n = nodo
        if (n == null || n.idNodo != nodoId) {
            MensajeVacio("Cargando ficha…")
        } else {
        val esFavorito = n.idNodo in favoritos
        val enRuta = borrador.any { it.idNodo == n.idNodo }

        Column(Modifier.verticalScroll(rememberScrollState())) {
            // Encabezado con la imagen principal simulada
            Box(Modifier.fillMaxWidth().height(180.dp)) {
                RecursoSimulado(
                    titulo = n.recursos.firstOrNull { it.esPrincipal }?.titulo ?: "Galería de fotos",
                    tipo = "IMAGEN",
                    modifier = Modifier.fillMaxSize()
                )
                Row(Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                    FilledIconButton(
                        onClick = {
                            if (usuario == null) dialogoInvitado = "Debes iniciar sesión para guardar favoritos."
                            else nodoVM.alternarFavorito(n.idNodo)
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White)
                    ) {
                        Icon(if (esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito", tint = if (esFavorito) Paleta.Rojo else Paleta.TextoSuave)
                    }
                }
            }

            Column(Modifier.padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Etiqueta(Formatos.tipoNodo(n.tipoNodo))
                    EtiquetaEstado(n.estadoActual)
                    if (n.estiloArquitectonico.isNotBlank()) Etiqueta(n.estiloArquitectonico, Paleta.AmbarSuave, Paleta.Ambar)
                }
                Spacer(Modifier.height(8.dp))
                Text(n.nombreOficial, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Paleta.Texto)
                Spacer(Modifier.height(4.dp))
                FilaDato(Icons.Default.LocationOn, "${n.direccion}, ${n.comuna}")
                if (n.decanato.isNotBlank()) FilaDato(Icons.Default.Church, n.decanato)
                n.anioConstruccion?.let { FilaDato(Icons.Default.HistoryEdu, "Construcción: $it") }

                // Aviso si hoy tiene un horario especial
                n.horariosExcepcionales.firstOrNull()?.let { h ->
                    Spacer(Modifier.height(8.dp))
                    Surface(color = Paleta.MoradoSuave, shape = MaterialTheme.shapes.medium) {
                        Text(
                            "Horario especial ${Formatos.fechaCorta(h.fecha)} ${h.horaInicio}-${h.horaFin}: " +
                                "${EstadosApertura.nombreVisible(h.estadoApertura)}. ${h.motivo}",
                            fontSize = 12.sp, color = Paleta.Morado, modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            TabRow(selectedTabIndex = pestana, containerColor = Color.White) {
                listOf("Historia", "Horarios", "Galería", "Eventos (${eventos.size})").forEachIndexed { i, t ->
                    Tab(selected = pestana == i, onClick = { pestana = i }, text = { Text(t, fontSize = 12.sp) })
                }
            }

            Column(Modifier.padding(16.dp)) {
                when (pestana) {
                    0 -> Text(n.resenaHistorica.ifBlank { "Sin reseña registrada." }, fontSize = 14.sp, color = Paleta.TextoMedio, lineHeight = 20.sp)
                    1 -> {
                        if (n.horariosRegulares.isEmpty()) Text("Sin horario registrado", color = Paleta.TextoSuave)
                        n.horariosRegulares.forEach { h ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text(Formatos.dia(h.diaSemana), Modifier.weight(1f), color = Paleta.TextoMedio)
                                Text("${h.horaApertura} - ${h.horaCierre}", fontWeight = FontWeight.SemiBold, color = Paleta.Texto)
                            }
                        }
                        if (n.horariosExcepcionales.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            TituloSeccion("Horarios excepcionales")
                            n.horariosExcepcionales.forEach { h ->
                                Text("• ${Formatos.fechaLarga(h.fecha)}, ${h.horaInicio}-${h.horaFin}: ${EstadosApertura.nombreVisible(h.estadoApertura)} (${h.motivo})",
                                    fontSize = 13.sp, color = Paleta.TextoMedio, modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                    2 -> {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(n.recursos) { r ->
                                Column(Modifier.width(140.dp)) {
                                    RecursoSimulado(r.titulo, r.tipoRecurso, Modifier.fillMaxWidth().height(100.dp))
                                    if (r.tipoRecurso == "AUDIO") {
                                        Text("Audioguía · ${Formatos.duracion((r.duracionSegundos ?: 0) / 60)}", fontSize = 11.sp, color = Paleta.TextoSuave)
                                    }
                                }
                            }
                        }
                        if (n.recursos.isEmpty()) Text("Sin recursos multimedia", color = Paleta.TextoSuave)
                        Text("Recursos multimedia simulados (URLs de prueba).", fontSize = 11.sp, color = Paleta.TextoSuave, modifier = Modifier.padding(top = 8.dp))
                    }
                    else -> {
                        if (eventos.isEmpty()) Text("No hay eventos próximos en este lugar", color = Paleta.TextoSuave)
                        eventos.forEach { e ->
                            Tarjeta(Modifier.padding(vertical = 4.dp)) {
                                Text(e.titulo, fontWeight = FontWeight.Bold, color = Paleta.Texto)
                                Text("${Formatos.categoriaEvento(e.categoria)} · ${Formatos.fechaHora(e.fechaInicio)}", fontSize = 12.sp, color = Paleta.TextoSuave)
                            }
                        }
                        if (eventos.isNotEmpty()) {
                            TextButton(onClick = { navController.navigate(AppScreens.EventList.route) }) { Text("Ir a la agenda para inscribirme") }
                        }
                    }
                }
            }

            // Acciones
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                BotonPrincipal(
                    texto = if (enRuta) "Quitar de mi ruta" else "Agregar a mi ruta",
                    icono = Icons.Default.AddLocationAlt,
                    onClick = { rutaVM.alternarParada(n) }
                )
                if (borrador.isNotEmpty()) {
                    OutlinedButton(onClick = { navController.navigate(AppScreens.CrearRuta.route) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Ver mi ruta (${borrador.size} lugares)")
                    }
                }
                if (usuario?.rol == Roles.PROFESOR) {
                    OutlinedButton(onClick = { navController.navigate(AppScreens.NuevaSalida.route) }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.School, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Agendar visita con mi curso")
                    }
                }
            }
        }
        }
    }

    dialogoInvitado?.let { texto ->
        DialogoInvitado(texto, onIniciarSesion = {
            dialogoInvitado = null
            navController.navigate(AppScreens.Login.route)
        }, onCerrar = { dialogoInvitado = null })
    }
}


// ---------- Vista previa para el panel Split de Android Studio (usa datos de prueba) ----------
@Preview(name = "Ficha patrimonial", showBackground = true, showSystemUi = true)
@Composable
private fun DetailScreenPreview() {
    val vm = rememberViewModelsPrueba(rol = Roles.TURISTA) {
        nodo.cargarDetalle(1); evento.cargarDeNodo(1)
    }
    VistaPrevia { DetailScreen(rememberNavController(), 1, vm.auth, vm.nodo, vm.evento, vm.ruta) }
}
