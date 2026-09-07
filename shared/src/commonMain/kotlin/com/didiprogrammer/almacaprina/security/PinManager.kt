package com.didiprogrammer.almacaprina.security

private const val MAX_PIN_ATTEMPTS = 5
private const val SALT_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

enum class PinVerifyResult { CORRECT, INCORRECT, LOCKED_OUT, NO_PIN }

/**
 * PIN de acceso rápido opcional (ver CLAUDE.md § Autenticación). Es solo un
 * candado local del dispositivo — NUNCA sustituye la autenticación real contra
 * Supabase. Únicamente tiene sentido cuando ya existe una sesión de Supabase
 * vigente: desbloquea la UI sobre esa sesión, no crea una nueva.
 */
object PinManager {
    fun hasPin(): Boolean = PinStorage.hasPin()

    fun setPin(pin: String) {
        val salt = randomSalt()
        PinStorage.savePin(hashPin(pin, salt), salt)
        PinStorage.setFailedAttempts(0)
    }

    fun clearPin() {
        PinStorage.clearPin()
    }

    /**
     * Verifica el PIN contra el hash guardado. Si se agotan los intentos
     * (ver [MAX_PIN_ATTEMPTS]), borra el PIN — la próxima apertura de la app
     * forzará login completo con contraseña.
     */
    fun verify(pin: String): PinVerifyResult {
        val (hash, salt) = PinStorage.readPin() ?: return PinVerifyResult.NO_PIN
        if (hashPin(pin, salt) == hash) {
            PinStorage.setFailedAttempts(0)
            return PinVerifyResult.CORRECT
        }
        val attempts = PinStorage.getFailedAttempts() + 1
        return if (attempts >= MAX_PIN_ATTEMPTS) {
            PinStorage.clearPin()
            PinVerifyResult.LOCKED_OUT
        } else {
            PinStorage.setFailedAttempts(attempts)
            PinVerifyResult.INCORRECT
        }
    }

    fun remainingAttempts(): Int = (MAX_PIN_ATTEMPTS - PinStorage.getFailedAttempts()).coerceAtLeast(0)

    private fun hashPin(pin: String, salt: String): String = sha256Hex("$salt:$pin")

    private fun randomSalt(): String = (1..16).map { SALT_CHARS.random() }.joinToString("")
}
