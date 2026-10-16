package com.arzobispado.redpatrimonial.viewmodel

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel(){
    //representa si el usuario navegando copmo invitado
    private  val _isGuest = MutableStateFlow(true)
    val isGuest: StateFlow<Boolean> = _isGuest.asStateFlow()

    // esto representa si la sesion es ta activa o iniciada
    private  val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn : StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun  LoginAsGuest(){

        _isGuest.value = true
        _isLoggedIn.value = false
    }
    fun loginUser(correo: String, clave : String): Boolean{

        //validacion de las credenciales
        if (correo.isNotBlank() && clave.length >=6){
            _isGuest.value = false
            _isLoggedIn.value =true
            return true

        }
        return false

    }
    fun logout(){
        _isGuest.value = true
        _isLoggedIn.value = false
    }





}