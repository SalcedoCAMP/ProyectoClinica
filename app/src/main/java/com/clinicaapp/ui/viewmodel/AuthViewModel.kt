package com.clinicaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.clinicaapp.data.db.entities.User
import com.clinicaapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para las operaciones de autenticación (login y registro).
 */
class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    // Estado del login
    private val _loginState = MutableStateFlow<AuthResult>(AuthResult.Idle)
    val loginState: StateFlow<AuthResult> = _loginState

    // Estado del registro
    private val _registrationState = MutableStateFlow<AuthResult>(AuthResult.Idle)
    val registrationState: StateFlow<AuthResult> = _registrationState

    /**
     Realiza la lógica de inicio de sesión.
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = AuthResult.Loading
            try {
                val user = userRepository.loginUser(email, password)
                if (user != null) {
                    _loginState.value = AuthResult.Success(user)
                } else {
                    _loginState.value = AuthResult.Error("Credenciales inválidas")
                }
            } catch (e: Exception) {
                _loginState.value = AuthResult.Error(e.localizedMessage ?: "Error desconocido al iniciar sesión")
            }
        }
    }

    /**
      Realiza la lógica de registro de un nuevo usuario.
     */
    fun register(name: String, dni: String, email: String, password: String) {
        viewModelScope.launch {
            _registrationState.value = AuthResult.Loading
            try {
                // Verificar si el correo ya está registrado
                val existingUser = userRepository.getUserByEmail(email)
                if (existingUser != null) {
                    _registrationState.value = AuthResult.Error("El correo electrónico ya está registrado.")
                    return@launch
                }

                val newUser = User(name = name, dni = dni, email = email, passwordHash = password)
                val userId = userRepository.registerUser(newUser)
                if (userId > 0) {
                    _registrationState.value = AuthResult.Success(newUser.copy(id = userId))
                } else {
                    _registrationState.value = AuthResult.Error("Fallo al registrar usuario")
                }
            } catch (e: Exception) {
                _registrationState.value = AuthResult.Error(e.localizedMessage ?: "Error desconocido al registrar")
            }
        }
    }

    /**
     * Restablece el estado de autenticación.
     */
    fun resetAuthState() {
        _loginState.value = AuthResult.Idle
        _registrationState.value = AuthResult.Idle
    }
}

/**
 * Clase sellada para representar los diferentes estados de un resultado de autenticación.
 */
sealed class AuthResult {
    object Idle : AuthResult()
    object Loading : AuthResult()
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/**
 * Factory para crear instancias de AuthViewModel con un UserRepository.
 */
class AuthViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}