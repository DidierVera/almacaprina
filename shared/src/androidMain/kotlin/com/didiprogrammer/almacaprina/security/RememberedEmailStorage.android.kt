package com.didiprogrammer.almacaprina.security

import android.content.Context

private const val PREFS_NAME = "almacaprina_login_prefs"
private const val PREF_EMAIL = "remembered_email"

/** Correo no es un dato sensible — se guarda en SharedPreferences normal, sin cifrar
 * (a diferencia del PIN, ver PinStorage.android.kt). */
actual object RememberedEmailStorage {
    private val prefs by lazy {
        AndroidAppContext.appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    actual fun saveEmail(email: String) {
        prefs.edit().putString(PREF_EMAIL, email).apply()
    }

    actual fun readEmail(): String? = prefs.getString(PREF_EMAIL, null)

    actual fun clearEmail() {
        prefs.edit().remove(PREF_EMAIL).apply()
    }
}
