package com.allinone.toolbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.allinone.toolbox.ui.navigation.Screen
import com.allinone.toolbox.ui.theme.AllInOneToolboxTheme
import com.allinone.toolbox.ui.hardware.HardwareScreen
import com.allinone.toolbox.ui.toolbox.ToolboxScreen
import com.allinone.toolbox.ui.firmware.FirmwareScreen
import com.allinone.toolbox.ui.profile.ProfileScreen
import com.allinone.toolbox.ui.home.HomeScreen
import com.allinone.toolbox.ui.toolbox.AppExtractScreen
import com.allinone.toolbox.ui.toolbox.AppInfoScreen
import com.allinone.toolbox.ui.toolbox.SystemShortcutsScreen
import com.allinone.toolbox.ui.profile.ActivateMemberScreen
import com.allinone.toolbox.ui.profile.AboutDeveloperScreen
import com.allinone.toolbox.ui.profile.AboutAppScreen
import com.allinone.toolbox.ui.theme.ThemeViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Android 15+: 启用Edge-to-Edge全面屏
        enableEdgeToEdge()
        setContent {
            val context = this
            val themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModel.Factory(context))
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()
            AllInOneToolboxTheme(darkTheme = isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainContent(themeViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(themeViewModel: ThemeViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavRoutes = listOf(
        Screen.Home.route,
        Screen.Toolbox.route,
        Screen.Firmware.route,
        Screen.Profile.route
    )

    Scaffold(
        topBar = {
            if (currentRoute in bottomNavRoutes) {
                TopAppBar(
                    title = {
                        Text(
                            when (currentRoute) {
                                Screen.Home.route -> "硬件检测"
                                Screen.Toolbox.route -> "工具箱"
                                Screen.Firmware.route -> "固件查询"
                                Screen.Profile.route -> "我的"
                                else -> "全能工具箱"
                            }
                        )
                    }
                )
            }
        },
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Screen.Home.route,
                        onClick = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route)
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "硬件检测") },
                        label = { Text("硬件") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Toolbox.route,
                        onClick = {
                            navController.navigate(Screen.Toolbox.route) {
                                popUpTo(Screen.Home.route)
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.Build, contentDescription = "工具箱") },
                        label = { Text("工具") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Firmware.route,
                        onClick = {
                            navController.navigate(Screen.Firmware.route) {
                                popUpTo(Screen.Home.route)
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.Download, contentDescription = "固件查询") },
                        label = { Text("固件") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Profile.route,
                        onClick = {
                            navController.navigate(Screen.Profile.route) {
                                popUpTo(Screen.Home.route)
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = "我的") },
                        label = { Text("我的") }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    paddingValues = paddingValues,
                    onNavigateToHardware = { navController.navigate(Screen.Hardware.route) }
                )
            }
            composable(Screen.Toolbox.route) {
                ToolboxScreen(
                    paddingValues = paddingValues,
                    onNavigateToAppExtract = { navController.navigate(Screen.AppExtract.route) },
                    onNavigateToAppInfo = { navController.navigate(Screen.AppInfo.route) },
                    onNavigateToSystemShortcuts = { navController.navigate(Screen.SystemShortcuts.route) }
                )
            }
            composable(Screen.Firmware.route) {
                FirmwareScreen(paddingValues = paddingValues)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    paddingValues = paddingValues,
                    themeViewModel = themeViewModel,
                    onNavigateToActivate = { navController.navigate(Screen.ActivateMember.route) },
                    onNavigateToAboutDeveloper = { navController.navigate(Screen.AboutDeveloper.route) },
                    onNavigateToAboutApp = { navController.navigate(Screen.AboutApp.route) }
                )
            }
            composable(Screen.Hardware.route) {
                HardwareScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AppExtract.route) {
                AppExtractScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AppInfo.route) {
                AppInfoScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.SystemShortcuts.route) {
                SystemShortcutsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.ActivateMember.route) {
                ActivateMemberScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AboutDeveloper.route) {
                AboutDeveloperScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AboutApp.route) {
                AboutAppScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
