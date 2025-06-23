package com.clinicaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.clinicaapp.navigation.AppScreens
import com.clinicaapp.ui.screens.AdminAppointmentsListScreen
import com.clinicaapp.ui.screens.AdminDashboardScreen
import com.clinicaapp.ui.screens.AppointmentsScreen
import com.clinicaapp.ui.screens.BookAppointmentScreen
import com.clinicaapp.ui.screens.DashboardScreen
import com.clinicaapp.ui.screens.DoctorListScreen
import com.clinicaapp.ui.screens.LoginScreen
import com.clinicaapp.ui.screens.ProfileScreen
import com.clinicaapp.ui.screens.RegisterScreen
import com.clinicaapp.ui.screens.AddEditDoctorScreen
import com.clinicaapp.ui.screens.DoctorManagementScreen
import com.clinicaapp.ui.theme.ClinicaAppTheme
import com.clinicaapp.ui.viewmodel.SharedViewModel

// N U E V A S I M P O R T A C I O N E S P A R A F A R M A C I A
import com.clinicaapp.ui.screens.PharmacyProductsListScreen
import com.clinicaapp.ui.screens.CartScreen
import com.clinicaapp.ui.screens.QrScannerScreen
import com.clinicaapp.ui.screens.AdminPharmacyProductsScreen // Asumo que esta es la pantalla de gestión de productos del admin
import com.clinicaapp.ui.screens.AddEditPharmacyProductScreen
import com.clinicaapp.ui.screens.PurchaseVouchersScreen


class MainActivity : ComponentActivity() {

    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClinicaAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ClinicaAppNavHost(sharedViewModel = sharedViewModel)
                }
            }
        }
    }
}

@Composable
fun ClinicaAppNavHost(sharedViewModel: SharedViewModel) {
    val navController = rememberNavController()

    val currentUser by sharedViewModel.currentUser.collectAsState()

    val startDestination = remember(currentUser) {
        when (currentUser?.role) {
            "admin" -> AppScreens.AdminDashboard
            "user" -> AppScreens.Dashboard
            else -> AppScreens.Login
        }
    }

    LaunchedEffect(startDestination) {
        if (navController.currentDestination?.route != startDestination) {
            navController.navigate(startDestination) {
                popUpTo(startDestination) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(navController = navController, startDestination = AppScreens.Login) {
        composable(AppScreens.Login) {
            LoginScreen(navController = navController, sharedViewModel = sharedViewModel)
        }
        composable(AppScreens.Register) {
            RegisterScreen(navController = navController, sharedViewModel = sharedViewModel)
        }
        composable(AppScreens.Dashboard) {
            DashboardScreen(navController = navController, sharedViewModel = sharedViewModel)
        }
        composable(AppScreens.DoctorsList) {
            DoctorListScreen(navController = navController)
        }

        // --- RUTA UNIFICADA PARA BookAppointmentScreen CON ARGUMENTO OPCIONAL ---
        composable(
            route = AppScreens.BookAppointmentRouteWithArg,
            arguments = listOf(navArgument("doctorId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getLong("doctorId") ?: -1L
            BookAppointmentScreen(navController = navController, sharedViewModel = sharedViewModel, doctorId = doctorId)
        }

        composable(AppScreens.Appointments) {
            AppointmentsScreen(navController = navController, sharedViewModel = sharedViewModel)
        }
        composable(AppScreens.Profile) {
            ProfileScreen(navController = navController, sharedViewModel = sharedViewModel)
        }

        // Rutas de Administrador
        composable(AppScreens.AdminDashboard) {
            AdminDashboardScreen(navController = navController, sharedViewModel = sharedViewModel)
        }
        composable(AppScreens.DoctorManagement) {
            DoctorManagementScreen(navController = navController)
        }
        // --- RUTA UNIFICADA PARA AddEditDoctorScreen CON ARGUMENTO OPCIONAL ---
        composable(
            route = AppScreens.AddEditDoctorWithId, // Siempre usa la ruta con {doctorId}
            arguments = listOf(navArgument("doctorId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getLong("doctorId") ?: -1L
            AddEditDoctorScreen(navController = navController, doctorId = doctorId)
        }

        // Ruta unificada a AdminAppointmentsListScreen con argumento opcional
        composable(
            route = AppScreens.AdminAppointmentsListForDoctor, // Siempre usa la ruta con {doctorId}
            arguments = listOf(navArgument("doctorId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getLong("doctorId") ?: -1L
            AdminAppointmentsListScreen(navController = navController, doctorId = doctorId)
        }

        // --- N U E V A S R U T A S P A R A F A R M A C I A (U S U A R I O) ---
        composable(AppScreens.PharmacyProductsList) {
            PharmacyProductsListScreen(navController = navController, sharedViewModel = sharedViewModel)
        }
        composable(AppScreens.Cart) {
            CartScreen(navController = navController, sharedViewModel = sharedViewModel)
        }
        composable(AppScreens.QrScanner) {
            QrScannerScreen(navController = navController)
        }

        // --- N U E V A S R U T A S P A R A A D M I N I S T R A D O R D E F A R M A C I A ---
        composable(AppScreens.AdminPharmacyProducts) {
            AdminPharmacyProductsScreen(navController = navController) // Asumo que esta pantalla no necesita sharedViewModel por ahora
        }
        composable(
            route = AppScreens.AddEditPharmacyProductWithId,
            arguments = listOf(navArgument("productId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getLong("productId") ?: -1L
            AddEditPharmacyProductScreen(navController = navController, productId = productId)
        }
        composable(AppScreens.PurchaseVouchers) {
            PurchaseVouchersScreen(navController = navController)
        }
    }
}