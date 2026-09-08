package com.didiprogrammer.almacaprina.security

import platform.Foundation.NSUserDefaults

private const val PREF_EMAIL = "remembered_email"

/** Correo no es un dato sensible — se guarda en NSUserDefaults normal, sin Keychain
 * (a diferencia del PIN, ver PinStorage.ios.kt). */
actual object RememberedEmailStorage {
    actual fun saveEmail(email: String) {
        NSUserDefaults.standardUserDefaults.setObject(email, PREF_EMAIL)
    }

    actual fun readEmail(): String? = NSUserDefaults.standardUserDefaults.stringForKey(PREF_EMAIL)

    actual fun clearEmail() {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(PREF_EMAIL)
    }
}
