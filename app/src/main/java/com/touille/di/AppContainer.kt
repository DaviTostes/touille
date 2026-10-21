package com.touille.di

import android.content.Context
import com.touille.data.TouilleDatabase
import com.touille.data.TouilleRepository
import com.touille.ui.Language
import com.touille.ui.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class LanguageStore(context: Context) {

    private val prefs = context.getSharedPreferences("touille", Context.MODE_PRIVATE)

    private val _language = MutableStateFlow(
        prefs.getString(KEY, null)?.let { stored ->
            Language.entries.firstOrNull { it.name == stored }
        } ?: deviceDefault()
    )

    val language: StateFlow<Language> = _language.asStateFlow()

    fun set(value: Language) {
        prefs.edit().putString(KEY, value.name).apply()
        _language.value = value
    }

    private fun deviceDefault(): Language =
        if (Locale.getDefault().language == Language.PT.code) Language.PT else Language.EN

    private companion object {
        const val KEY = "language"
    }
}

class AppContainer(context: Context) {
    private val database = TouilleDatabase.build(context.applicationContext)
    val repository = TouilleRepository(database.dao())
    val languageStore = LanguageStore(context.applicationContext)
    val themeStore = ThemeStore(context.applicationContext)
}

class ThemeStore(context: Context) {

    private val prefs = context.getSharedPreferences("touille", Context.MODE_PRIVATE)

    private val _mode = MutableStateFlow(
        prefs.getString(KEY, null)?.let { stored ->
            ThemeMode.entries.firstOrNull { it.name == stored }
        } ?: ThemeMode.SYSTEM
    )

    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()

    fun set(value: ThemeMode) {
        prefs.edit().putString(KEY, value.name).apply()
        _mode.value = value
    }

    private companion object {
        const val KEY = "theme_mode"
    }
}
