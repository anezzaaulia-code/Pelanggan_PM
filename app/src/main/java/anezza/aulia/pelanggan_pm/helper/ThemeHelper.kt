package anezza.aulia.pelanggan_pm.helper

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {

    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"
    const val THEME_SYSTEM = "system"

    private const val PREF_NAME = "THEME_PREF"
    private const val KEY_THEME = "selected_theme"

    fun applyTheme(context: Context) {
        val selectedMode = when (getTheme(context)) {
            THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            THEME_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else -> AppCompatDelegate.MODE_NIGHT_NO
        }

        // Penting: jangan set ulang kalau mode-nya sudah sama
        if (AppCompatDelegate.getDefaultNightMode() != selectedMode) {
            AppCompatDelegate.setDefaultNightMode(selectedMode)
        }
    }

    fun saveTheme(context: Context, theme: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, theme)
            .apply()

        applyTheme(context)
    }

    fun getTheme(context: Context): String {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME, THEME_LIGHT) ?: THEME_LIGHT
    }
}