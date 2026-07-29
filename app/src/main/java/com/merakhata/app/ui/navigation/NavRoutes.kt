package com.merakhata.app.ui.navigation

sealed class NavRoutes(val route: String) {
    object Onboarding : NavRoutes("onboarding")
    object PinLock : NavRoutes("pin_lock")
    object Auth : NavRoutes("auth")
    object Home : NavRoutes("home")
    object Reports : NavRoutes("reports")
    object Reminders : NavRoutes("reminders")
    object Settings : NavRoutes("settings")

    object CustomerDetail : NavRoutes("customer_detail/{customerId}") {
        fun createRoute(customerId: Long) = "customer_detail/$customerId"
    }

    object AddEditCustomer : NavRoutes("add_edit_customer?customerId={customerId}") {
        fun createRoute(customerId: Long? = null) =
            if (customerId != null) "add_edit_customer?customerId=$customerId" else "add_edit_customer"
    }

    object AddEditTransaction : NavRoutes("add_edit_transaction/{customerId}?type={type}&txId={txId}") {
        fun createRoute(customerId: Long, type: String = "YOU_GAVE", txId: Long? = null) =
            "add_edit_transaction/$customerId?type=$type" + (if (txId != null) "&txId=$txId" else "")
    }
}
