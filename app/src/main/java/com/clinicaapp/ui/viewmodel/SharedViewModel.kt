package com.clinicaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.clinicaapp.data.db.entities.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedViewModel : ViewModel() {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun setCurrentUser(user: User?) {
        _currentUser.value = user
    }

    fun clearCurrentUser() {
        _currentUser.value = null
    }
}