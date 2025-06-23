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

/**
   ViewModel del perfil: carga y actualiza datos del usuario.
 */
class ProfileViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _currentUserData = MutableStateFlow<User?>(null)
    val currentUserData: StateFlow<User?> = _currentUserData.asStateFlow()

    private val _updateMessage = MutableStateFlow<String?>(null)
    val updateMessage: StateFlow<String?> = _updateMessage.asStateFlow()

    /**
     * Carga los datos del usuario actual.
     */
    fun loadUserData(userId: Long) {
        viewModelScope.launch {
            _currentUserData.value = userRepository.getUserById(userId)
        }
    }

    /**
     * Actualiza la información del usuario.
     */
    fun updateUserData(user: User) {
        viewModelScope.launch {
            try {
                val success = userRepository.updateUser(user)
                if (success) {
                    _currentUserData.value = user // Actualiza el Flow tras guardado exitoso
                    _updateMessage.value = "Perfil actualizado exitosamente."
                } else {
                    _updateMessage.value = "Error al actualizar perfil."
                }
            } catch (e: Exception) {
                _updateMessage.value = e.localizedMessage ?: "Error desconocido al actualizar."
            }
        }
    }

    /**
     * Limpia el mensaje de actualización.
     */
    fun clearUpdateMessage() {
        _updateMessage.value = null
    }
}

/**
 * Factory para crear instancias
 */
class ProfileViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}