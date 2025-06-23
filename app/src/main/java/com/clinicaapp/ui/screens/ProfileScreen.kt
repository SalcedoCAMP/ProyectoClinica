package com.clinicaapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.clinicaapp.data.db.AppDatabase
import com.clinicaapp.data.db.entities.User
import com.clinicaapp.data.repository.UserRepository
import com.clinicaapp.ui.viewmodel.ProfileViewModel
import com.clinicaapp.ui.viewmodel.ProfileViewModelFactory
import com.clinicaapp.ui.viewmodel.SharedViewModel

/**
 * Pantalla de perfil de usuario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, sharedViewModel: SharedViewModel) {
    val context = LocalContext.current
    val userRepository = remember { UserRepository(AppDatabase.getDatabase(context).userDao()) }
    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(userRepository))

    val currentUser by sharedViewModel.currentUser.collectAsState()
    val userId = currentUser?.id ?: -1L

    val userData by profileViewModel.currentUserData.collectAsState()
    val updateMessage by profileViewModel.updateMessage.collectAsState()

    var name by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") } // Solo para mostrar, no editable

    // Cargar los datos del usuario al iniciar la pantalla
    LaunchedEffect(userId) {
        if (userId != -1L) {
            profileViewModel.loadUserData(userId)
        }
    }

    // Actualiza los campos al cambiar los datos del usuario
    LaunchedEffect(userData) {
        userData?.let { user ->
            name = user.name
            dni = user.dni
            email = user.email
        }
    }

    // Observar el mensaje de actualización
    LaunchedEffect(updateMessage) {
        updateMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            profileViewModel.clearUpdateMessage()
            // Si la actualización fue exitosa, actualiza el usuario en SharedViewModel
            if (it.contains("exitosamente")) {
                userData?.let { updatedUser ->
                    sharedViewModel.setCurrentUser(updatedUser)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Información de Perfil",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre Completo") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Nombre") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            )

            OutlinedTextField(
                value = dni,
                onValueChange = { dni = it },
                label = { Text("DNI") },
                leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = "DNI") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = false
            )

            OutlinedTextField(
                value = email,
                onValueChange = { /* No se puede editar el email desde aquí */ },
                label = { Text("Correo Electrónico") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Correo") },
                readOnly = true, // El correo no es editable directamente
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (currentUser != null) {
                        val updatedUser = currentUser!!.copy(
                            name = name,
                            dni = dni
                        )
                        profileViewModel.updateUserData(updatedUser)
                    } else {
                        Toast.makeText(context, "No hay usuario logueado para actualizar.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Actualizar Perfil", fontSize = 18.sp)
            }
        }
    }
}