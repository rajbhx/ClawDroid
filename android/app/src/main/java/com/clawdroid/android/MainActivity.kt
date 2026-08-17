package com.clawdroid.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.activity.viewModels
import com.clawdroid.android.ui.navigation.ClawdroidNavGraph
import com.clawdroid.android.ui.theme.ClawdroidTheme
import com.clawdroid.android.ui.theme.ThemeMode
import com.clawdroid.android.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by settingsViewModel.themeMode.collectAsState(initial = 1)
            ClawdroidTheme(
                themeMode = if (themeMode == 0) ThemeMode.MATERIAL_YOU else ThemeMode.TOKYO_NIGHT,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    ClawdroidNavGraph()
                }
            }
        }
    }
}
