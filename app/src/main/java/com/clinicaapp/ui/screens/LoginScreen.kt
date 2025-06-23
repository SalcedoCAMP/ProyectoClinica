package com.clinicaapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.clinicaapp.data.db.AppDatabase
import com.clinicaapp.data.repository.UserRepository
import com.clinicaapp.navigation.AppScreens
import com.clinicaapp.ui.viewmodel.LoginViewModel
import com.clinicaapp.ui.viewmodel.LoginViewModelFactory
import com.clinicaapp.ui.viewmodel.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, sharedViewModel: SharedViewModel) {
    val context = LocalContext.current
    val userRepository = remember { UserRepository(AppDatabase.getDatabase(context).userDao()) }
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory(userRepository))

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val loginMessage by loginViewModel.loginMessage.collectAsState()
    val loginSuccess by loginViewModel.loginSuccess.collectAsState()

    LaunchedEffect(loginMessage) {
        loginMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            loginViewModel.clearLoginMessage()
        }
    }

    // LaunchedEffect para navegar tras login exitoso

    LaunchedEffect(loginSuccess) {
        loginSuccess?.let { user ->
            sharedViewModel.setCurrentUser(user) // Guarda en SharedViewModel
            // Navegar según el rol del usuario
            if (user.role == "admin") {
                navController.navigate(AppScreens.AdminDashboard) {
                    popUpTo(AppScreens.Login) { inclusive = true }
                }
            } else {
                navController.navigate(AppScreens.Dashboard) {
                    popUpTo(AppScreens.Login) { inclusive = true }
                }
            }
            loginViewModel.clearLoginSuccess() // Reinicia el estado tras navegar
        }
    }
   //

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Inicio de Sesión",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { loginViewModel.loginUser(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Iniciar Sesión", fontSize = 18.sp)
            }

            TextButton(
                onClick = { navController.navigate(AppScreens.Register) },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("¿No tienes cuenta? Regístrate aquí")
            }
        }
    }
}