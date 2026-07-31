package com.allinone.toolbox.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Hardware : Screen("hardware")
    data object Toolbox : Screen("toolbox")
    data object Firmware : Screen("firmware")
    data object Profile : Screen("profile")
    data object AppExtract : Screen("app_extract")
    data object AppInfo : Screen("app_info")
    data object SystemShortcuts : Screen("system_shortcuts")
    data object ActivateMember : Screen("activate_member")
    data object AboutDeveloper : Screen("about_developer")
    data object AboutApp : Screen("about_app")
}
