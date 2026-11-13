package com.arzobispado.redpatrimonial.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.arzobispado.redpatrimonial.ui.components.*
import com.arzobispado.redpatrimonial.viewmodel.EventoViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

/**
 * Pantalla "Control de entrada" (guía o encargado).
 *
 * Flujo completo (3 pasos):
 *  1) El usuario toca "Abrir cámara" -> se pide el permiso CAMERA (uso de recurso del teléfono).
 *  2) Si acepta, se abre el lector de QR (librería zxing-android-embedded) que devuelve el texto leído.
 *  3) Se llama a eventoVM.validarEntrada(codigo), que llama al backend y marca la asistencia.
 */
@Composable
fun EscanearEntradaScreen(navController: NavHostController, eventoVM: EventoViewModel) {
    // Estado de la pantalla: último código leído y si el permiso fue rechazado.
    var ultimoCodigo by remember { mutableStateOf<String?>(null) }
    var sinPermiso by remember { mutableStateOf(false) }

    // Paso 3 (se ejecuta al volver del lector): recibe el resultado del escaneo.
    // resultado.contents es null si el usuario canceló, por eso se usa "let".
    val lector = rememberLauncherForActivityResult(ScanContract()) { resultado ->
        resultado.contents?.let { codigo ->
            ultimoCodigo = codigo
            eventoVM.validarEntrada(codigo)
        }
    }

    // Paso 1-2: pide el permiso de cámara. Si se concede, abre el lector.
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        sinPermiso = !concedido
        if (concedido) lector.launch(opcionesLector())
    }

    // Pantalla() es el marco común de la app (título, volver, mensajes de error o éxito).
    Pantalla(titulo = "Control de entrada", onVolver = { navController.popBackStack() }, viewModels = listOf(eventoVM)) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(72.dp))
            Spacer(Modifier.height(12.dp))
            Text("Escanea el código QR del visitante", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                "Al leerlo, la entrada queda registrada como asistencia.",
                textAlign = TextAlign.Center, fontSize = 13.sp
            )
            Spacer(Modifier.height(20.dp))
            BotonPrincipal("Abrir cámara", onClick = { pedirPermiso.launch(Manifest.permission.CAMERA) },
                icono = Icons.Default.CameraAlt)
            // Mensaje si el permiso fue rechazado
            if (sinPermiso) {
                Spacer(Modifier.height(12.dp))
                Text("Necesitas aceptar el permiso de cámara para escanear.", color = Paleta.Rojo, fontSize = 13.sp)
            }
            // Muestra el último código leído (útil para verificar en la demostración)
            ultimoCodigo?.let {
                Spacer(Modifier.height(12.dp))
                Text("Último código leído: $it", fontSize = 13.sp, color = Paleta.TextoSuave)
            }
        }
    }
}

/** Opciones del lector: solo códigos QR, con sonido al leer y sin bloquear la orientación. */
private fun opcionesLector(): ScanOptions = ScanOptions()
    .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
    .setPrompt("Apunta la cámara al código QR")
    .setBeepEnabled(true)
    .setOrientationLocked(false)
