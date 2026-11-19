package com.example.businessos.data.security

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PinSecurity {
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    private val random = SecureRandom()

    fun generateSalt(): String {
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }

    fun hashPin(pin: String, saltHex: String): String {
        val saltBytes = saltHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        val spec = PBEKeySpec(pin.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(pin: String, saltHex: String, expectedHashHex: String): Boolean {
        if (saltHex.isEmpty() || expectedHashHex.isEmpty()) return false
        val computed = hashPin(pin, saltHex)
        return computed == expectedHashHex
    }
}
