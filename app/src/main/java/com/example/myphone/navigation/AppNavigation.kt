package com.example.myphone.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myphone.ui.screens.AddContactScreen
import com.example.myphone.ui.screens.CallHistoryScreen
import com.example.myphone.ui.screens.ContactDetailsScreen
import com.example.myphone.ui.screens.ContactsScreen
import com.example.myphone.ui.screens.DialerScreen
import com.example.myphone.ui.screens.EditContactScreen
import com.example.myphone.ui.screens.HomeScreen
import com.example.myphone.ui.screens.RecentsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Recents : Screen("recents")
    object Dialer : Screen("keypad")
    object Contacts : Screen("contacts")
    // NEW: Two separate, distinct routes for Add and Edit.
    object AddContact : Screen("add_contact")
    object EditContact : Screen("edit_contact/{contactId}") {
        fun createRoute(contactId: String) = "edit_contact/$contactId"
    }
    // NEW: A route for the call history screen for a specific number.
    object CallHistory : Screen("call_history/{phoneNumber}") {
        fun createRoute(phoneNumber: String) = "call_history/$phoneNumber"
    }
    object ContactDetails : Screen("contacts/{contactId}") {
        fun createRoute(contactId: String) = "contacts/$contactId"
    }
}

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route, "Home", Icons.Default.Home),
    BottomNavItem(Screen.Recents.route, "Recents", Icons.Default.History),
    BottomNavItem(Screen.Dialer.route, "Keypad", Icons.Default.Dialpad),
    BottomNavItem(Screen.Contacts.route, "Contacts", Icons.Default.Person)
)
data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { AppBottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Recents.route) { RecentsScreen(
                navController = navController
            ) }
            composable(Screen.Dialer.route) { DialerScreen() }
            composable(Screen.Contacts.route) {
                ContactsScreen(navController = navController)
            }
            // NEW: Separate composable destinations.
            composable(Screen.AddContact.route) {
                AddContactScreen(navController = navController)
            }
            composable(
                route = Screen.EditContact.route,
                arguments = listOf(navArgument("contactId") { type = NavType.StringType })
            ) { backStackEntry ->
                EditContactScreen(
                    navController = navController,
                    backStackEntry = backStackEntry
                )
            }
            // NEW: Add the call history screen to the navigation graph.
            // For now, it will show a placeholder.
            composable(
                route = Screen.CallHistory.route,
                arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
            ) {
                // Replace the placeholder with the real screen.
                CallHistoryScreen(navController = navController)
            }
            composable(
                route = Screen.ContactDetails.route,
                arguments = listOf(navArgument("contactId") {
                    type = NavType.StringType
                })
            ) {
                ContactDetailsScreen(navController = navController)
            }
        }
    }
}

@Composable
fun AppBottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        bottomNavItems.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

