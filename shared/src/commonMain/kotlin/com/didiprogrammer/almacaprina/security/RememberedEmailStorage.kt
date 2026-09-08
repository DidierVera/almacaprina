package com.didiprogrammer.almacaprina.security

/**
 * Recuerda el correo del último inicio de sesión exitoso — solo el correo, NUNCA la
 * contraseña (ver CLAUDE.md § Autenticación). Pensado para cuando una misma persona alterna
 * entre varios roles/cuentas en el mismo dispositivo: evita reescribir el correo cada vez sin
 * guardar ningún dato sensible.
 */
expect object RememberedEmailStorage {
    fun saveEmail(email: String)
    fun readEmail(): String?
    fun clearEmail()
}
