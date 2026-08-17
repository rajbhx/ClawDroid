package com.clawdroid.android.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.clawdroid.android.ui.screens.AgentHubScreen
import com.clawdroid.android.ui.screens.ChatScreen
import com.clawdroid.android.ui.screens.MemoryScreen
import com.clawdroid.android.ui.screens.SettingsScreen
import com.clawdroid.android.ui.screens.TerminalScreen

data class NavItem(val screen: Screen, val label: String, val icon: ImageVector)

val navItems = listOf(
    NavItem(Screen.Chat, "Chat", Icons.Default.Chat),
    NavItem(Screen.Terminal, "Terminal", Icons.Default.Code),
    NavItem(Screen.Agents, "Agents", Icons.Default.Router),
    NavItem(Screen.Memory, "Memory", Icons.Default.Memory),
    NavItem(Screen.Settings, "Settings", Icons.Default.Settings),
)

@Composable
fun ClawdroidNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                navItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Chat.route,
            modifier = Modifier.padding(paddingValues),
        ) {
            composable(Screen.Chat.route) { ChatScreen() }
            composable(Screen.Terminal.route) { TerminalScreen() }
            composable(Screen.Agents.route) { AgentHubScreen() }
            composable(Screen.Memory.route) { MemoryScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
