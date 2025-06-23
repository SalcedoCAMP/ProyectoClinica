package com.clinicaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.clinicaapp.data.db.entities.User
import com.clinicaapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _loginMessage = MutableStateFlow<String?>(null)
    val loginMessage: StateFlow<String?> = _loginMessage.asStateFlow()

    // Indica el éxito del login y el usuario autenticado
    private val _loginSuccess = MutableStateFlow<User?>(null)
    val loginSuccess: StateFlow<User?> = _loginSuccess.asStateFlow()

    fun loginUser(email: String, passwordRaw: String) { // Se compara antes de hashear
        viewModelScope.launch {
            val user = userRepository.getUserByEmail(email)
            if (user != null && user.passwordHash == passwordRaw) {
                _loginSuccess.value = user // Establece el usuario logueado
                _loginMessage.value = "Inicio de sesión exitoso."
            } else {
                _loginMessage.value = "Credenciales incorrectas."
            }
        }
    }

    fun clearLoginMessage() {
        _loginMessage.value = null
    }

    fun clearLoginSuccess() { // Limpia estado de éxito tras navegar
        _loginSuccess.value = null
    }
}

class LoginViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}