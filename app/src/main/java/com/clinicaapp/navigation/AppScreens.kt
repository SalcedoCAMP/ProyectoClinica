package com.clinicaapp.navigation

object AppScreens {
    const val Login = "login_screen"
    const val Register = "register_screen"
    const val Dashboard = "dashboard_screen"
    const val AdminDashboard = "admin_dashboard_screen"

    const val DoctorsList = "doctors_list_screen"

    // Rutas para la pantalla de reserva de citas
    const val BookAppointmentBase = "book_appointment_screen"
    const val BookAppointmentRouteWithArg = "$BookAppointmentBase/{doctorId}"

    fun bookAppointmentRoute(doctorId: Long = -1L): String {
        return "$BookAppointmentBase/$doctorId"
    }

    const val Appointments = "appointments_screen" // Citas del usuario

    const val Profile = "profile_screen"

    // Rutas para la administración de doctores
    const val DoctorManagement = "doctor_management_screen"
    const val AddEditDoctorBase = "add_edit_doctor_screen"
    const val AddEditDoctorWithId = "$AddEditDoctorBase/{doctorId}"

    fun addEditDoctorRoute(doctorId: Long = -1L): String {
        return "$AddEditDoctorBase/$doctorId"
    }

    // Rutas para la administración de citas (por administrador)
    const val AdminAppointmentsListBase = "admin_appointments_list_screen"
    const val AdminAppointmentsListForDoctor = "$AdminAppointmentsListBase/{doctorId}"

    fun adminAppointmentsForDoctorRoute(doctorId: Long = -1L): String {
        return "$AdminAppointmentsListBase/$doctorId"
    }

    //RUTAS PARA FARMACIA (USUARIO)
    const val PharmacyProductsList = "pharmacy_products_list_screen"
    const val Cart = "cart_screen"
    const val QrScanner = "qr_scanner_screen" // Para escanear productos o pagos

    // RUTAS PARA ADMINISTRADOR DE FARMACIA
    const val AdminPharmacyProducts = "admin_pharmacy_products_screen" // Renombrado de AdminPharmacyManagement para mayor claridad con el Composable.
    const val AddEditPharmacyProductBase = "add_edit_pharmacy_product_screen"
    const val AddEditPharmacyProductWithId = "$AddEditPharmacyProductBase/{productId}"
    const val PurchaseVouchers = "purchase_vouchers_screen" // Para ver todas las compras

    fun addEditPharmacyProductRoute(productId: Long = -1L): String {
        return "$AddEditPharmacyProductBase/$productId"
    }
}