package com.didiprogrammer.almacaprina.security

/**
 * Candado local de PIN (ver CLAUDE.md § Autenticación) — almacenamiento seguro
 * específico de plataforma (Keystore en Android, Keychain en iOS). Guarda solo
 * el hash + salt del PIN, nunca el PIN en texto plano, y el contador de intentos
 * fallidos para el bloqueo tras varios intentos.
 *
 * También guarda el `userId` (id de Supabase Auth) del usuario dueño del PIN —
 * el almacenamiento solo tiene un slot por dispositivo, así que si otro usuario
 * inicia sesión en el mismo dispositivo (ej. durante pruebas con varios roles),
 * [PinManager.hasPin] compara contra este valor en vez de asumir que cualquier
 * PIN guardado le pertenece.
 */
expect object PinStorage {
    fun savePin(hash: String, salt: String, userId: String)
    fun readPin(): Pair<String, String>?
    fun readUserId(): String?
    fun clearPin()
    fun hasPin(): Boolean
    fun getFailedAttempts(): Int
    fun setFailedAttempts(count: Int)
}
