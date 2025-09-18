package com.example.myphone.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myphone.ui.screens.ContactsScreen
import com.example.myphone.ui.screens.DialerScreen
import com.example.myphone.ui.screens.HomeScreen
import com.example.myphone.ui.screens.RecentsScreen

// --- Navigation Routes and Destinations ---

// A sealed class makes our navigation type-safe. We can't accidentally navigate
// to a route that doesn't exist.
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Dialer : Screen("keypad", "Keypad", Icons.Default.Call)
    object Contacts : Screen("contacts", "Contacts", Icons.Default.Person)
}

// A list of our primary screens for easy access in the BottomNavBar
val bottomNavItems = listOf(
    Screen.Home,
    Screen.Dialer,
    Screen.Contacts
)

// --- The Main Navigation Composable ---

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { AppBottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        // NavHost is the container for our different screens
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Define a composable for each screen in our app
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Dialer.route) { DialerScreen() }
            composable(Screen.Contacts.route) { ContactsScreen() }
        }
    }
}

// --- Reusable Bottom Navigation Bar ---

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
