package com.merakhata.app.domain.security

import java.security.MessageDigest

object SecurityManager {
    private const val SALT = "MERA_KHATA_SECURE_SALT_2026"

    fun hashPin(pin: String): String {
        val input = "$SALT:$pin"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(pin: String, storedHash: String?): Boolean {
        if (storedHash == null) return false
        return hashPin(pin) == storedHash
    }
}
