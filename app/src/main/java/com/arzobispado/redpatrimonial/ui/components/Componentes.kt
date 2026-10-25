package com.arzobispado.redpatrimonial.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arzobispado.redpatrimonial.data.model.EstadosApertura
import com.arzobispado.redpatrimonial.data.model.EstadosSalida
import com.arzobispado.redpatrimonial.data.model.NodoPatrimonial
import com.arzobispado.redpatrimonial.data.repository.EstadoConexion
import com.arzobispado.redpatrimonial.util.Formatos
import com.arzobispado.redpatrimonial.viewmodel.BaseViewModel

/** Colores de la marca (los mismos que ya usaban las pantallas). */
object Paleta {
    val Marino = Color(0xFF0F172A)
    val AzulOscuro = Color(0xFF1E3A8A)
    val Azul = Color(0xFF2563EB)
    val AzulTexto = Color(0xFF1E40AF)
    val AzulSuave = Color(0xFFDBEAFE)
    val AzulMuySuave = Color(0xFFEFF6FF)
    val Celeste = Color(0xFF93C5FD)
    val Fondo = Color(0xFFF1F5F9)
    val Texto = Color(0xFF0F172A)
    val TextoMedio = Color(0xFF334155)
    val TextoSuave = Color(0xFF64748B)
    val Borde = Color(0xFFE2E8F0)
    val Verde = Color(0xFF166534)
    val VerdeSuave = Color(0xFFDCFCE7)
    val Rojo = Color(0xFFDC2626)
    val RojoOscuro = Color(0xFF991B1B)
    val RojoSuave = Color(0xFFFEE2E2)
    val Ambar = Color(0xFF92400E)
    val AmbarSuave = Color(0xFFFEF3C7)
    val Morado = Color(0xFF6B21A8)
    val MoradoSuave = Color(0xFFF3E8FF)
}

/**
 * Estructura común de las pantallas: barra superior, mensajes (Snackbar),
 * indicador de carga y aviso de modo sin conexión.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Pantalla(
    titulo: String,
    subtitulo: String? = null,
    onVolver: (() -> Unit)? = null,
    viewModels: List<BaseViewModel> = emptyList(),
    acciones: @Composable RowScope.() -> Unit = {},
    fab: @Composable () -> Unit = {},
    contenido: @Composable ColumnScope.() -> Unit
) {
    val snackbar = remember { SnackbarHostState() }
    var cargando = false
    viewModels.forEach { vm ->
        val mensaje by vm.mensaje.collectAsState()
        val c by vm.cargando.collectAsState()
        cargando = cargando || c
        LaunchedEffect(mensaje) {
            mensaje?.let {
                snackbar.showSnackbar(it)
                vm.limpiarMensaje()
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = fab,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(titulo, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (subtitulo != null) Text(subtitulo, fontSize = 12.sp, color = Paleta.Celeste, maxLines = 1)
                    }
                },
                navigationIcon = {
                    if (onVolver != null) {
                        IconButton(onClick = onVolver) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                        }
                    }
                },
                actions = acciones,
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Paleta.Marino)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Paleta.Fondo)
        ) {
            if (cargando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Paleta.Azul)
            BannerSinConexion()
            contenido()
        }
    }
}

/** Avisa cuando los microservicios no responden y se están usando datos de prueba. */
@Composable
fun BannerSinConexion() {
    val enLinea by EstadoConexion.enLinea.collectAsState()
    if (!enLinea) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Paleta.AmbarSuave)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CloudOff, contentDescription = null, tint = Paleta.Ambar, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Sin conexión al servidor: mostrando datos de prueba", fontSize = 12.sp, color = Paleta.Ambar)
        }
    }
}

@Composable
fun Etiqueta(texto: String, fondo: Color = Paleta.AzulSuave, color: Color = Paleta.AzulTexto) {
    Surface(color = fondo, shape = RoundedCornerShape(6.dp)) {
        Text(texto, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
    }
}

/** Etiqueta de color según el estado de apertura (Abierto / Cerrado / En celebración). */
@Composable
fun EtiquetaEstado(estado: String) {
    val (fondo, color) = when (estado) {
        EstadosApertura.ABIERTO -> Paleta.VerdeSuave to Paleta.Verde
        EstadosApertura.EN_CELEBRACION -> Paleta.MoradoSuave to Paleta.Morado
        else -> Paleta.RojoSuave to Paleta.RojoOscuro
    }
    Etiqueta("● ${EstadosApertura.nombreVisible(estado)}", fondo, color)
}

/** Etiqueta para estados de salidas y asignaciones de guía. */
@Composable
fun EtiquetaSalida(estado: String) {
    val (fondo, color) = when (estado) {
        EstadosSalida.CONFIRMADA, EstadosSalida.ACEPTADA -> Paleta.VerdeSuave to Paleta.Verde
        EstadosSalida.RECHAZADA -> Paleta.RojoSuave to Paleta.RojoOscuro
        EstadosSalida.FINALIZADA -> Paleta.AzulSuave to Paleta.AzulTexto
        EstadosSalida.SIN_GUIA -> Paleta.Borde to Paleta.TextoMedio
        else -> Paleta.AmbarSuave to Paleta.Ambar
    }
    Etiqueta(EstadosSalida.nombreVisible(estado), fondo, color)
}

fun colorEstado(estado: String): Color = when (estado) {
    EstadosApertura.ABIERTO -> Color(0xFF16A34A)
    EstadosApertura.EN_CELEBRACION -> Color(0xFF9333EA)
    else -> Paleta.Rojo
}

fun iconoTipo(tipo: String): ImageVector = when (tipo) {
    "MUSEO" -> Icons.Default.Museum
    "COLEGIO" -> Icons.Default.School
    "ARCHIVO" -> Icons.Default.Description
    else -> Icons.Default.Church
}

@Composable
fun TituloSeccion(texto: String, modifier: Modifier = Modifier) {
    Text(texto, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Paleta.Texto, modifier = modifier)
}

/** Tarjeta de un templo/nodo usada en el mapa, la búsqueda y la creación de rutas. */
@Composable
fun TarjetaNodo(
    nodo: NodoPatrimonial,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    extremoDerecho: @Composable () -> Unit = {}
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(Paleta.AzulMuySuave, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconoTipo(nodo.tipoNodo), contentDescription = null, tint = Paleta.Azul)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(nodo.nombreOficial, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Paleta.Texto,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Paleta.TextoSuave, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("${nodo.comuna} · ${Formatos.tipoNodo(nodo.tipoNodo)}", fontSize = 12.sp, color = Paleta.TextoSuave,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(6.dp))
                EtiquetaEstado(nodo.estadoActual)
            }
            extremoDerecho()
        }
    }
}

@Composable
fun MensajeVacio(texto: String, icono: ImageVector = Icons.Default.Info) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icono, contentDescription = null, tint = Paleta.TextoSuave, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(8.dp))
        Text(texto, color = Paleta.TextoSuave, textAlign = TextAlign.Center, fontSize = 14.sp)
    }
}

/** Diálogo que pide iniciar sesión cuando un invitado intenta una acción protegida. */
@Composable
fun DialogoInvitado(texto: String, onIniciarSesion: () -> Unit, onCerrar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCerrar,
        icon = { Icon(Icons.Default.Lock, contentDescription = null) },
        title = { Text("Función para usuarios registrados") },
        text = { Text(texto) },
        confirmButton = { TextButton(onClick = onIniciarSesion) { Text("Iniciar sesión") } },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } }
    )
}

@Composable
fun CampoFormulario(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    ayuda: String? = null,
    teclado: KeyboardType = KeyboardType.Text,
    lineas: Int = 1
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        leadingIcon = if (icono != null) { { Icon(icono, contentDescription = null) } } else null,
        supportingText = if (ayuda != null) { { Text(ayuda) } } else null,
        singleLine = lineas == 1,
        minLines = lineas,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
        modifier = modifier.fillMaxWidth()
    )
}

/** Fila de opciones exclusivas (por ejemplo, categoría o estado). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorOpciones(
    opciones: List<String>,
    seleccionada: String?,
    texto: (String) -> String,
    onSeleccionar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.lazy.LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(opciones.size) { i ->
            val op = opciones[i]
            FilterChip(
                selected = op == seleccionada,
                onClick = { onSeleccionar(op) },
                label = { Text(texto(op)) },
                leadingIcon = if (op == seleccionada) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
            )
        }
    }
}

/** Botón principal azul de ancho completo. */
@Composable
fun BotonPrincipal(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier, icono: ImageVector? = null, habilitado: Boolean = true, color: Color = Paleta.Azul) {
    Button(
        onClick = onClick,
        enabled = habilitado,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        if (icono != null) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(texto, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

/** Tarjeta blanca genérica con padding. */
@Composable
fun Tarjeta(modifier: Modifier = Modifier, contenido: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), content = contenido)
    }
}

/** Fila con ícono y texto (dirección, horario, etc.). */
@Composable
fun FilaDato(icono: ImageVector, texto: String, color: Color = Paleta.TextoMedio, tinte: Color = Paleta.Azul) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(icono, contentDescription = null, tint = tinte, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(texto, fontSize = 13.sp, color = color)
    }
}
