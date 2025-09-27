package com.example.myphone.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.myphone.ui.screens.*
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Home : Screen("home")
//    object Recents : Screen("recents")
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
    object Settings : Screen("settings")
}

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route, "Home", Icons.Default.Home),
//    BottomNavItem(Screen.Recents.route, "Recents", Icons.Default.History),
    BottomNavItem(Screen.Dialer.route, "Keypad", Icons.Default.Dialpad),
    BottomNavItem(Screen.Contacts.route, "Contacts", Icons.Default.Person)
)
data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Settings.route)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            bottomBar = { AppBottomNavigationBar(navController = navController) }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        navController = navController,
                        onMenuClick = {
                            scope.launch { drawerState.open() }
                        }
                    )
                }
                composable(Screen.Dialer.route) { DialerScreen() }
                composable(Screen.Contacts.route) {
                    ContactsScreen(navController = navController)
                }
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
                composable(
                    route = Screen.CallHistory.route,
                    arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
                ) {
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
                composable(Screen.Settings.route) {
                    SettingsScreen(navController = navController)
                }
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

