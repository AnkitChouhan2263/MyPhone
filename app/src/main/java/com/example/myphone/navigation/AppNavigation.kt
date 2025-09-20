package com.example.myphone.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
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
import com.example.myphone.ui.screens.ContactDetailsScreen
import com.example.myphone.ui.screens.ContactsScreen
import com.example.myphone.ui.screens.DialerScreen
import com.example.myphone.ui.screens.HomeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Dialer : Screen("keypad")
    object Contacts : Screen("contacts")
    object ContactDetails : Screen("contacts/{contactId}") {
        fun createRoute(contactId: String) = "contacts/$contactId"
    }
}

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route, "Home", Icons.Default.Home),
    BottomNavItem(Screen.Dialer.route, "Keypad", Icons.Default.Call),
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
            composable(Screen.Dialer.route) { DialerScreen() }
            composable(Screen.Contacts.route) {
                ContactsScreen(navController = navController)
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

