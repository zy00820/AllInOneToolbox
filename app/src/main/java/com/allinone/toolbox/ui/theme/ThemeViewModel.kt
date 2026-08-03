package com.allinone.toolbox.ui.theme

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.allinone.toolbox.utils.ActivationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeViewModel(private val context: Context) : ViewModel() {

    private val _isDarkMode = MutableStateFlow(getSavedDarkMode())
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    /** 兼容旧接口：LITE/PRO 都算会员 */
    private val _isMember = MutableStateFlow(ActivationUtils.isMember())
    val isMember: StateFlow<Boolean> = _isMember.asStateFlow()

    /** 会员等级：NONE / LITE / PRO */
    private val _memberLevel = MutableStateFlow(ActivationUtils.memberLevel())
    val memberLevel: StateFlow<ActivationUtils.MemberLevel> = _memberLevel.asStateFlow()

    private fun getSavedDarkMode(): Boolean {
        val prefs = context.getSharedPreferences("all_in_one_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("dark_mode", false)
    }

    fun setDarkMode(isDark: Boolean) {
        _isDarkMode.value = isDark
        val prefs = context.getSharedPreferences("all_in_one_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("dark_mode", isDark).apply()
    }

    /** 兼容接口，保留 */
    fun setMemberStatus(isMember: Boolean) {
        _isMember.value = isMember
        _memberLevel.value = ActivationUtils.memberLevel()
    }

    /** 激活状态变更后刷新 */
    fun refreshMemberStatus() {
        _isMember.value = ActivationUtils.isMember()
        _memberLevel.value = ActivationUtils.memberLevel()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ThemeViewModel(context) as T
        }
    }
}
