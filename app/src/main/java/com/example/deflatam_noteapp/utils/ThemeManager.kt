package com.example.deflatam_noteapp.utils


import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/**
 * Objeto para gestionar la persistencia y aplicación del tema (claro/oscuro).
 * Usa SharedPreferences para guardar la elección del usuario.
 */
object ThemeManager {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME = "selected_theme"

    // Constantes para los modos de tema
    const val THEME_LIGHT = 0
    const val THEME_DARK = 1
    const val THEME_SYSTEM = 2

    /**
     * Aplica el tema guardado al iniciar la aplicación.
     * Debe llamarse en el onCreate de la Activity principal.
     */
    fun applyTheme(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val selectedTheme = prefs.getInt(KEY_THEME, THEME_SYSTEM)
        setTheme(selectedTheme)
    }

    /**
     * Guarda la selección de tema del usuario y la aplica inmediatamente.
     * @param themeMode Una de las constantes: THEME_LIGHT, THEME_DARK, THEME_SYSTEM.
     */
    fun setTheme(context: Context, themeMode: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_THEME, themeMode).apply()
        setTheme(themeMode)
    }

    /**
     * Cambia el modo de la aplicación usando AppCompatDelegate.
     * Es la función que realmente hace el cambio visual.
     */
    private fun setTheme(themeMode: Int) {
        when (themeMode) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}