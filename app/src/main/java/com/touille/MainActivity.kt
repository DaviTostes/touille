package com.touille

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.touille.ui.LocalLanguage
import com.touille.ui.LocalStrings
import com.touille.ui.ThemeMode
import com.touille.ui.TouilleApp
import com.touille.ui.stringsFor
import com.touille.ui.theme.TouilleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = (application as TouilleApplication).container
        setContent {
            val language by container.languageStore.language.collectAsStateWithLifecycle()
            val themeMode by container.themeStore.mode.collectAsStateWithLifecycle()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            SideEffect {
                // Keep status/navigation bar icons readable when the app theme differs from the system one.
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }

            TouilleTheme(darkTheme = darkTheme) {
                CompositionLocalProvider(
                    LocalLanguage provides language,
                    LocalStrings provides stringsFor(language)
                ) {
                    TouilleApp(container.repository, container.languageStore, container.themeStore)
                }
            }
        }
    }
}
