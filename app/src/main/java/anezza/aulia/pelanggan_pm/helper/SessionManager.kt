package anezza.aulia.pelanggan_pm.helper

import android.content.Context

class SessionManager(context: Context) {

    private val pref = context.getSharedPreferences("PELANGGAN_PM_SESSION", Context.MODE_PRIVATE)
    private val editor = pref.edit()

    fun saveLogin(token: String, id: Int, name: String, email: String, telepon: String) {
        editor.putBoolean("is_login", true)
        editor.putString("token", token)
        editor.putInt("id", id)
        editor.putString("name", name)
        editor.putString("email", email)
        editor.putString("telepon", telepon)
        editor.apply()
    }

    fun isLogin(): Boolean {
        return pref.getBoolean("is_login", false)
    }

    fun token(): String {
        return pref.getString("token", "") ?: ""
    }

    fun name(): String {
        return pref.getString("name", "") ?: ""
    }

    fun email(): String {
        return pref.getString("email", "") ?: ""
    }

    fun telepon(): String {
        return pref.getString("telepon", "") ?: ""
    }

    fun logout() {
        editor.clear()
        editor.apply()
    }
}