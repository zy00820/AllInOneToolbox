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
    // V1.1.0 新增：真正的 Shizuku 授权管理页（修复伪授权问题）
    data object ShizukuPermission : Screen("shizuku_permission")

    // V1.0.11 新增：品牌专属板块
    data object XiaomiSection : Screen("xiaomi_section")
    data object VivoSection : Screen("vivo_section")
    data object OppoSection : Screen("oppo_section")
    data object SamsungSection : Screen("samsung_section")
    data object HuaweiSection : Screen("huawei_section")

    // V1.0.11 新增：高级工具
    data object BatteryModify : Screen("battery_modify")
    data object SceneActivate : Screen("scene_activate")
}
