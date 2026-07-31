package com.allinone.toolbox.ui.theme

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(private val context: Context) : ViewModel() {

    private val _isDarkMode = MutableStateFlow(getSavedDarkMode())
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isMember = MutableStateFlow(getSavedMemberStatus())
    val isMember: StateFlow<Boolean> = _isMember.asStateFlow()

    private fun getSavedDarkMode(): Boolean {
        val prefs = context.getSharedPreferences("all_in_one_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("dark_mode", false)
    }

    private fun getSavedMemberStatus(): Boolean {
        val prefs = context.getSharedPreferences("all_in_one_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_member", false)
    }

    fun setDarkMode(isDark: Boolean) {
        _isDarkMode.value = isDark
        val prefs = context.getSharedPreferences("all_in_one_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("dark_mode", isDark).apply()
    }

    fun setMemberStatus(isMember: Boolean) {
        _isMember.value = isMember
        val prefs = context.getSharedPreferences("all_in_one_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_member", isMember).apply()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ThemeViewModel(context) as T
        }
    }
}
