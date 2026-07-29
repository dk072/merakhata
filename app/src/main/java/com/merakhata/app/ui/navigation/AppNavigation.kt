package com.merakhata.app.ui.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.merakhata.app.MeraKhataApp
import com.merakhata.app.ui.screens.*
import com.merakhata.app.ui.theme.EmeraldPrimary
import com.merakhata.app.ui.viewmodels.*

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem(NavRoutes.Home.route, "Home", Icons.Default.Home)
    object Reports : BottomNavItem(NavRoutes.Reports.route, "Reports", Icons.Default.BarChart)
    object Reminders : BottomNavItem(NavRoutes.Reminders.route, "Reminders", Icons.Default.Notifications)
    object Settings : BottomNavItem(NavRoutes.Settings.route, "Settings", Icons.Default.Settings)
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val app = context.applicationContext as MeraKhataApp
    val repository = app.repository

    val isOnboardingCompleted by repository.preferences.isOnboardingCompleted.collectAsState(initial = true)
    val isAppLockEnabled by repository.preferences.isAppLockEnabled.collectAsState(initial = false)

    val securityViewModel = remember { SecurityViewModel(repository) }
    val isUnlocked by securityViewModel.isUnlocked.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Reports,
        BottomNavItem.Reminders,
        BottomNavItem.Settings
    )

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    // Lock check on app launch
    val startDestination = when {
        isAppLockEnabled && !isUnlocked -> NavRoutes.PinLock.route
        !isOnboardingCompleted -> NavRoutes.Onboarding.route
        else -> NavRoutes.Home.route
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 10.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.height(72.dp)
                    ) {
                        bottomNavItems.forEach { item ->
                            val isSelected = currentRoute == item.route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(item.icon, contentDescription = item.title) },
                                label = {
                                    Text(
                                        text = item.title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoutes.Onboarding.route) {
                val vm = remember { OnboardingViewModel(repository) }
                OnboardingScreen(
                    viewModel = vm,
                    onComplete = {
                        navController.navigate(NavRoutes.Home.route) {
                            popUpTo(NavRoutes.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(NavRoutes.PinLock.route) {
                PinLockScreen(
                    viewModel = securityViewModel,
                    onUnlocked = {
                        val target = if (!isOnboardingCompleted) NavRoutes.Onboarding.route else NavRoutes.Home.route
                        navController.navigate(target) {
                            popUpTo(NavRoutes.PinLock.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(NavRoutes.Home.route) {
                val vm = remember { HomeViewModel(repository) }
                HomeScreen(
                    viewModel = vm,
                    onNavigateToCustomer = { customerId ->
                        navController.navigate(NavRoutes.CustomerDetail.createRoute(customerId))
                    },
                    onNavigateToAddCustomer = {
                        navController.navigate(NavRoutes.AddEditCustomer.createRoute())
                    }
                )
            }

            composable(NavRoutes.Reports.route) {
                val vm = remember { ReportsViewModel(repository) }
                ReportsScreen(viewModel = vm)
            }

            composable(NavRoutes.Reminders.route) {
                val vm = remember { RemindersViewModel(repository) }
                RemindersScreen(viewModel = vm)
            }

            composable(NavRoutes.Settings.route) {
                val vm = remember { SettingsViewModel(repository) }
                SettingsScreen(viewModel = vm)
            }

            composable(
                route = NavRoutes.CustomerDetail.route,
                arguments = listOf(navArgument("customerId") { type = NavType.LongType })
            ) { backStackEntry ->
                val customerId = backStackEntry.arguments?.getLong("customerId") ?: return@composable
                val vm = remember(customerId) { CustomerDetailViewModel(repository, customerId) }
                CustomerDetailScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEditCustomer = { id ->
                        navController.navigate(NavRoutes.AddEditCustomer.createRoute(id))
                    },
                    onNavigateToAddTransaction = { custId, type, txId ->
                        navController.navigate(NavRoutes.AddEditTransaction.createRoute(custId, type, txId))
                    }
                )
            }

            composable(
                route = NavRoutes.AddEditCustomer.route,
                arguments = listOf(navArgument("customerId") {
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) { backStackEntry ->
                val custId = backStackEntry.arguments?.getLong("customerId")?.takeIf { it > 0 }
                val vm = remember(custId) { AddEditCustomerViewModel(repository, custId) }
                AddEditCustomerScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = NavRoutes.AddEditTransaction.route,
                arguments = listOf(
                    navArgument("customerId") { type = NavType.LongType },
                    navArgument("type") {
                        type = NavType.StringType
                        defaultValue = "YOU_GAVE"
                    },
                    navArgument("txId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val customerId = backStackEntry.arguments?.getLong("customerId") ?: return@composable
                val type = backStackEntry.arguments?.getString("type")
                val txId = backStackEntry.arguments?.getLong("txId")?.takeIf { it > 0 }
                val vm = remember(customerId, type, txId) {
                    AddEditTransactionViewModel(repository, customerId, type, txId)
                }
                AddEditTransactionScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
