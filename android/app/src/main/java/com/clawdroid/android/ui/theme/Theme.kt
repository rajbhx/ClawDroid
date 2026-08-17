package com.clawdroid.android.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext

enum class ThemeMode { MATERIAL_YOU, TOKYO_NIGHT }

private val TokyoNightDark = darkColorScheme(
    primary = TokyoNightPrimary,
    onPrimary = TokyoNightOnPrimary,
    secondary = TokyoNightSecondary,
    tertiary = TokyoNightTertiary,
    background = TokyoNightBackground,
    surface = TokyoNightSurface,
    surfaceVariant = TokyoNightSurfaceVariant,
    onBackground = TokyoNightOnBackground,
    onSurface = TokyoNightOnSurface,
    error = TokyoNightError,
    onError = TokyoNightOnError,
    outline = TokyoNightOutline,
)

@Composable
fun ClawdroidTheme(
    themeMode: ThemeMode = ThemeMode.TOKYO_NIGHT,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (themeMode) {
        ThemeMode.MATERIAL_YOU -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                dynamicDarkColorScheme(context)
            } else {
                TokyoNightDark
            }
        }
        ThemeMode.TOKYO_NIGHT -> TokyoNightDark
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ClawdroidTypography,
        content = content,
    )
}
