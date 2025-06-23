package com.clinicaapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.clinicaapp.navigation.AppScreens
import com.clinicaapp.data.repository.UserRepository
import com.clinicaapp.data.db.AppDatabase
import com.clinicaapp.ui.viewmodel.AuthResult
import com.clinicaapp.ui.viewmodel.AuthViewModel
import com.clinicaapp.ui.viewmodel.AuthViewModelFactory
import com.clinicaapp.ui.viewmodel.SharedViewModel

/**
 * Pantalla de registro para la aplicación ClinicaApp.
 * Permite a los nuevos usuarios crear una cuenta con su nombre, DNI, correo y contraseña.
 *
 * @param navController El NavController para la navegación.
 * @param sharedViewModel El SharedViewModel para actualizar el usuario actual.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController, sharedViewModel: SharedViewModel) {
    val context = LocalContext.current
    val userRepository = remember { UserRepository(AppDatabase.getDatabase(context).userDao()) }
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(userRepository))

    var name by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val registrationState by authViewModel.registrationState.collectAsState()

    // Observar el estado del registro
    LaunchedEffect(registrationState) {
        when (registrationState) {
            is AuthResult.Success -> {
                val user = (registrationState as AuthResult.Success).user
                sharedViewModel.setCurrentUser(user)
                Toast.makeText(context, "¡Registro exitoso! Bienvenido, ${user.name}!", Toast.LENGTH_SHORT).show()
                navController.navigate(AppScreens.Dashboard) {
                    popUpTo(AppScreens.Login) { inclusive = true }
                }
                authViewModel.resetAuthState()
            }
            is AuthResult.Error -> {
                val message = (registrationState as AuthResult.Error).message
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                authViewModel.resetAuthState() // Reset state after showing error
            }
            AuthResult.Loading -> {
            }
            AuthResult.Idle -> {
                // Est inicial o reseteado
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Usuario") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.Lock, contentDescription = "Atrás")
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
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Crea tu cuenta",
                fontSize = 28.sp,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 32.dp)
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
                shape = RoundedCornerShape(8.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo Electrónico") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Correo") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { authViewModel.register(name, dni, email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = registrationState != AuthResult.Loading
            ) {
                if (registrationState == AuthResult.Loading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Registrarse", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.popBackStack() }) {
                Text("¿Ya tienes cuenta? Inicia sesión", color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}