package com.didiprogrammer.almacaprina.security

/**
 * Candado local de PIN (ver CLAUDE.md § Autenticación) — almacenamiento seguro
 * específico de plataforma (Keystore en Android, Keychain en iOS). Guarda solo
 * el hash + salt del PIN, nunca el PIN en texto plano, y el contador de intentos
 * fallidos para el bloqueo tras varios intentos.
 */
expect object PinStorage {
    fun savePin(hash: String, salt: String)
    fun readPin(): Pair<String, String>?
    fun clearPin()
    fun hasPin(): Boolean
    fun getFailedAttempts(): Int
    fun setFailedAttempts(count: Int)
}
