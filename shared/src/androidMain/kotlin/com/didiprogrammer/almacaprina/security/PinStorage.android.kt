package com.didiprogrammer.almacaprina.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val PREFS_NAME = "almacaprina_pin_prefs"
private const val KEY_ALIAS = "almacaprina_pin_key"
private const val PREF_HASH = "pin_hash_enc"
private const val PREF_SALT = "pin_salt_enc"
private const val PREF_ATTEMPTS = "pin_failed_attempts"
private const val PREF_USER_ID = "pin_user_id"
private const val GCM_TAG_LENGTH_BITS = 128
private const val GCM_IV_LENGTH_BYTES = 12

/**
 * Guarda el hash+salt del PIN cifrados con una clave AES-GCM generada y
 * custodiada por el Android Keystore (la clave nunca sale del hardware de
 * seguridad del dispositivo) — ver CLAUDE.md § Autenticación.
 */
actual object PinStorage {
    private val prefs by lazy {
        AndroidAppContext.appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    actual fun savePin(hash: String, salt: String, userId: String) {
        prefs.edit()
            .putString(PREF_HASH, encrypt(hash))
            .putString(PREF_SALT, encrypt(salt))
            .putString(PREF_USER_ID, userId)
            .putInt(PREF_ATTEMPTS, 0)
            .apply()
    }

    actual fun readPin(): Pair<String, String>? {
        val encHash = prefs.getString(PREF_HASH, null) ?: return null
        val encSalt = prefs.getString(PREF_SALT, null) ?: return null
        return decrypt(encHash) to decrypt(encSalt)
    }

    actual fun readUserId(): String? = prefs.getString(PREF_USER_ID, null)

    actual fun clearPin() {
        prefs.edit().remove(PREF_HASH).remove(PREF_SALT).remove(PREF_USER_ID).putInt(PREF_ATTEMPTS, 0).apply()
    }

    actual fun hasPin(): Boolean = prefs.contains(PREF_HASH)

    actual fun getFailedAttempts(): Int = prefs.getInt(PREF_ATTEMPTS, 0)

    actual fun setFailedAttempts(count: Int) {
        prefs.edit().putInt(PREF_ATTEMPTS, count).apply()
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        keyGenerator.init(
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return keyGenerator.generateKey()
    }

    private fun encrypt(plain: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plain.encodeToByteArray())
        return Base64.encodeToString(iv + ciphertext, Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): String {
        val combined = Base64.decode(encoded, Base64.NO_WRAP)
        val iv = combined.copyOfRange(0, GCM_IV_LENGTH_BYTES)
        val ciphertext = combined.copyOfRange(GCM_IV_LENGTH_BYTES, combined.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        return cipher.doFinal(ciphertext).decodeToString()
    }
}
