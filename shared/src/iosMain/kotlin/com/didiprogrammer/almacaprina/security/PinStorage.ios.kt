package com.didiprogrammer.almacaprina.security

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSUserDefaults
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.posix.memcpy

private const val SERVICE = "com.didiprogrammer.almacaprina.pin"
private const val ACCOUNT_HASH = "pin_hash"
private const val ACCOUNT_SALT = "pin_salt"
private const val PREF_ATTEMPTS = "pin_failed_attempts"
private const val PREF_USER_ID = "pin_user_id"

/**
 * Guarda el hash+salt del PIN en el Keychain de iOS (cifrado por el sistema en
 * reposo) — ver CLAUDE.md § Autenticación. El contador de intentos fallidos
 * (dato no sensible) va en NSUserDefaults.
 */
@OptIn(ExperimentalForeignApi::class)
actual object PinStorage {
    actual fun savePin(hash: String, salt: String, userId: String) {
        keychainSet(ACCOUNT_HASH, hash)
        keychainSet(ACCOUNT_SALT, salt)
        NSUserDefaults.standardUserDefaults.setObject(userId, PREF_USER_ID)
        setFailedAttemptsInternal(0)
    }

    actual fun readPin(): Pair<String, String>? {
        val hash = keychainGet(ACCOUNT_HASH) ?: return null
        val salt = keychainGet(ACCOUNT_SALT) ?: return null
        return hash to salt
    }

    actual fun readUserId(): String? = NSUserDefaults.standardUserDefaults.stringForKey(PREF_USER_ID)

    actual fun clearPin() {
        keychainDelete(ACCOUNT_HASH)
        keychainDelete(ACCOUNT_SALT)
        NSUserDefaults.standardUserDefaults.removeObjectForKey(PREF_USER_ID)
        setFailedAttemptsInternal(0)
    }

    actual fun hasPin(): Boolean = keychainGet(ACCOUNT_HASH) != null

    actual fun getFailedAttempts(): Int =
        NSUserDefaults.standardUserDefaults.integerForKey(PREF_ATTEMPTS).toInt()

    actual fun setFailedAttempts(count: Int) = setFailedAttemptsInternal(count)

    private fun setFailedAttemptsInternal(count: Int) {
        NSUserDefaults.standardUserDefaults.setInteger(count.toLong(), PREF_ATTEMPTS)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun baseQuery(account: String): CFMutableDictionaryRef {
    val query = CFDictionaryCreateMutable(null, 0, null, null)!!
    CFDictionarySetValue(query, kSecClass, kSecClassGenericPassword)
    CFDictionarySetValue(query, kSecAttrService, CFBridgingRetain(SERVICE))
    CFDictionarySetValue(query, kSecAttrAccount, CFBridgingRetain(account))
    return query
}

@OptIn(ExperimentalForeignApi::class)
private fun stringToCFData(value: String): CFDataRef? {
    val bytes = value.encodeToByteArray()
    return bytes.usePinned { pinned ->
        CFDataCreate(null, pinned.addressOf(0).reinterpret(), bytes.size.convert())
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun keychainSet(account: String, value: String) {
    keychainDelete(account)
    val query = baseQuery(account)
    CFDictionarySetValue(query, kSecValueData, stringToCFData(value))
    SecItemAdd(query, null)
}

@OptIn(ExperimentalForeignApi::class)
private fun keychainGet(account: String): String? {
    val query = baseQuery(account)
    CFDictionarySetValue(query, kSecReturnData, kCFBooleanTrue)
    CFDictionarySetValue(query, kSecMatchLimit, kSecMatchLimitOne)
    return memScoped {
        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query, result.ptr)
        if (status != errSecSuccess) return@memScoped null
        val dataRef: CFDataRef = (result.value ?: return@memScoped null).reinterpret()
        try {
            val length = CFDataGetLength(dataRef).toInt()
            val bytePtr = CFDataGetBytePtr(dataRef)
            val bytes = ByteArray(length)
            if (length > 0 && bytePtr != null) {
                bytes.usePinned { pinned -> memcpy(pinned.addressOf(0), bytePtr, length.convert()) }
            }
            bytes.decodeToString()
        } finally {
            CFRelease(dataRef)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun keychainDelete(account: String) {
    SecItemDelete(baseQuery(account))
}
