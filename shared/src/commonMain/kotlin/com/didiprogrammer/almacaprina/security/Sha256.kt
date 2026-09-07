package com.didiprogrammer.almacaprina.security

/** Hash SHA-256 en hexadecimal — usado solo para el candado local de PIN, nunca para contraseñas reales. */
expect fun sha256Hex(input: String): String
