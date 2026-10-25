package com.arzobispado.redpatrimonial.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.arzobispado.redpatrimonial.data.model.CategoriasAviso
import com.arzobispado.redpatrimonial.ui.components.*
import com.arzobispado.redpatrimonial.viewmodel.AuthViewModel
import com.arzobispado.redpatrimonial.viewmodel.AvisoViewModel
import com.arzobispado.redpatrimonial.viewmodel.NodoViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.arzobispado.redpatrimonial.ui.preview.VistaPrevia
import com.arzobispado.redpatrimonial.ui.preview.rememberViewModelsPrueba
import com.arzobispado.redpatrimonial.data.model.Roles

/** CU14: publicar un aviso que se ve de inmediato en toda la red (modelo tipo SoSafe). */
@Composable
fun PublicarAvisoScreen(navController: NavHostController, authVM: AuthViewModel, nodoVM: NodoViewModel, avisoVM: AvisoViewModel) {
    val usuario by authVM.usuario.collectAsState()
    val nodos by nodoVM.nodos.collectAsState()
    val nodo = nodos.firstOrNull { it.idNodo == usuario?.idNodoAsignado }
    var categoria by remember { mutableStateOf(CategoriasAviso.APERTURA_ESPECIAL) }
    var titulo by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    // Vigencia en horas
    val vigencias = listOf("3", "12", "24", "72", "168")
    var vigencia by remember { mutableStateOf("24") }

    Pantalla(titulo = "Publicar aviso en la red", subtitulo = nodo?.nombreOficial, onVolver = { navController.popBackStack() }, viewModels = listOf(avisoVM)) {
        if (nodo == null || usuario == null) {
            MensajeVacio("No tienes un templo asignado")
        } else {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Categoría del aviso", fontSize = 12.sp, color = Paleta.TextoSuave)
                SelectorOpciones(CategoriasAviso.TODAS, categoria, { "${CategoriasAviso.icono(it)} ${CategoriasAviso.nombreVisible(it)}" }, { categoria = it })
                CampoFormulario(titulo, { titulo = it.take(100) }, "Título del aviso", icono = Icons.Default.Title, ayuda = "${titulo.length}/100")
                CampoFormulario(mensaje, { mensaje = it }, "Detalle del evento o aviso público", lineas = 4)
                Text("Expiración del aviso", fontSize = 12.sp, color = Paleta.TextoSuave)
                SelectorOpciones(vigencias, vigencia, { if (it == "168") "1 semana" else "$it horas" }, { vigencia = it })

                // Vista previa
                Text("Vista previa", fontSize = 12.sp, color = Paleta.TextoSuave, modifier = Modifier.padding(top = 6.dp))
                TarjetaAviso(
                    aviso = com.arzobispado.redpatrimonial.data.model.Aviso(
                        0, nodo.idNodo, nodo.nombreOficial, usuario!!.idUsuario, categoria, CategoriasAviso.icono(categoria),
                        titulo.ifBlank { "Título del aviso" }, mensaje.ifBlank { "Detalle del aviso" },
                        java.time.LocalDateTime.now().toString(),
                        java.time.LocalDateTime.now().plusHours(vigencia.toLong()).toString(), 0, estado = "ACTIVO", vigente = true
                    ),
                    onTemplo = {}
                )

                BotonPrincipal("Publicar inmediatamente", icono = Icons.Default.Campaign, color = Paleta.Rojo, onClick = {
                    avisoVM.publicar(nodo, usuario!!, categoria, titulo, mensaje, vigencia.toInt()) { navController.popBackStack() }
                })
            }
        }
    }
}


// ---------- Vista previa para el panel Split de Android Studio (usa datos de prueba) ----------
@Preview(name = "Publicar aviso", showBackground = true, showSystemUi = true)
@Composable
private fun PublicarAvisoScreenPreview() {
    val vm = rememberViewModelsPrueba(rol = Roles.ENCARGADO)
    VistaPrevia { PublicarAvisoScreen(rememberNavController(), vm.auth, vm.nodo, vm.aviso) }
}
