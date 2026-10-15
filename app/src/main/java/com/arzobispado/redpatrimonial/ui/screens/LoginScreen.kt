package com.arzobispado.redpatrimonial.ui.screens

import android.R
import android.view.View
import android.widget.NumberPicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Volcano
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.arzobispado.redpatrimonial.navigation.AppScreens

// Pantalla principal de Autenticación de Usuario
@Composable
fun LoginScreen(navController: NavController){
    // esgtados locales en comoose para guardar lo que escribe el usuario
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var contrsenaVisible by remember { mutableStateOf(false) }


    //contenedor principal
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E3A8A))
                )
            )

    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center

        ){

            Icon(
                imageVector = Icons.Default.AccountBalance,
                contentDescription = "Logo Red Patrimonial",
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF93C5FD)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Red Patrimonial ",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White

            )
            Text(
                text ="Arzobispado de Santiago",
                fontSize = 14.sp,
                color = Color(0xFF93C5FD)


            )
            Spacer(modifier = Modifier.height(32.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()

            ) {
                Column(modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                    Text(
                        text = "BIENVENIDO",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text="INICIA SESSION",
                        fontSize =  12.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = correo,
                        onValueChange = {correo = it},
                        label = {Text("CORREO ELECTRONICO")},
                        leadingIcon = {Icon(Icons.Default.Email, contentDescription = null)},
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(25.dp)


                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(

                        value = contrasena,
                        onValueChange = {contrasena= it},
                        label = {Text ("CONTRACEÑA")},
                        leadingIcon = {Icon(Icons.Default.Lock , contentDescription = null)},
                        trailingIcon = {
                            IconButton(onClick = {contrsenaVisible = !contrsenaVisible}) {
                                Icon(imageVector = if (contrsenaVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "OCULTAR O MOSTRAR CONTRACEÑA")

                            }
                        },
                       visualTransformation = if (contrsenaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(25.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            // Navega hacia la pantalla del Mapa
                            navController.navigate(AppScreens.Map.route) {
                                popUpTo(AppScreens.Login.route) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text(
                            text = "Iniciar Sesión",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Botón Secundario: Modo Invitado
                    OutlinedButton(
                        onClick = {
                            navController.navigate(AppScreens.Map.route)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = " Explorar como Invitado",
                            color = Color(0xFF334155)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Enlace a la pantalla de Registro
                    TextButton(
                        onClick = {
                            navController.navigate(AppScreens.Register.route)
                        }
                    ) {
                        Text(
                            text = "¿No tienes cuenta? Regístrate aquí",
                            fontSize = 13.sp,
                            color = Color(0xFF2563EB),
                            fontWeight = FontWeight.Bold
                        )
                    }




                }

            }




        }

    }




}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = rememberNavController())
}