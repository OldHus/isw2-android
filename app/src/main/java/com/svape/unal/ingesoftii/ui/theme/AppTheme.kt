package com.svape.unal.ingesoftii.ui.theme

import android.app.UiModeManager
import android.content.Context
import android.graphics.Color as AndroidColor
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemePreferences private constructor(private val appContext: Context) {
    private val prefs = appContext.getSharedPreferences("ui_prefs", Context.MODE_PRIVATE)
    private val _mode = MutableStateFlow(read())
    val themeMode: StateFlow<ThemeMode> = _mode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY, mode.name).apply()
        _mode.value = mode
        applyToSystem(mode)
    }

    fun applyToSystem(mode: ThemeMode = _mode.value) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val uiModeManager = appContext.getSystemService(UiModeManager::class.java) ?: return
        val target = when (mode) {
            ThemeMode.System -> UiModeManager.MODE_NIGHT_AUTO
            ThemeMode.Light -> UiModeManager.MODE_NIGHT_NO
            ThemeMode.Dark -> UiModeManager.MODE_NIGHT_YES
        }
        runCatching { uiModeManager.setApplicationNightMode(target) }
    }

    private fun read(): ThemeMode =
        prefs.getString(KEY, null)?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.System

    companion object {
        private const val KEY = "theme_mode"

        @Volatile
        private var instance: ThemePreferences? = null

        fun get(context: Context): ThemePreferences =
            instance ?: synchronized(this) {
                instance ?: ThemePreferences(context.applicationContext).also { instance = it }
            }
    }
}

@Composable
fun IngesoftiiApp(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { ThemePreferences.get(context) }
    val mode by prefs.themeMode.collectAsState()
    val isDark = when (mode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    LaunchedEffect(Unit) { prefs.applyToSystem() }

    val activity = context as? ComponentActivity
    DisposableEffect(isDark) {
        activity?.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                AndroidColor.TRANSPARENT,
                AndroidColor.TRANSPARENT
            ) { isDark },
            navigationBarStyle = SystemBarStyle.auto(
                AndroidColor.TRANSPARENT,
                AndroidColor.TRANSPARENT
            ) { isDark },
        )
        onDispose { }
    }

    IngesoftiiTheme(darkTheme = isDark) {
        ThemeRevealHost(
            onToggleTheme = { prefs.setThemeMode(if (isDark) ThemeMode.Light else ThemeMode.Dark) },
        ) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                content()
            }
        }
    }
}