package com.clawdroid.android.ui.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Chat : Screen("chat")
    data object Terminal : Screen("terminal")
    data object Agents : Screen("agents")
    data object Memory : Screen("memory")
    data object Settings : Screen("settings")
}
