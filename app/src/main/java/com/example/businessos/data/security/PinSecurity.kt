package com.example.businessos.data.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Salted, slow PIN hashing. PINs are never stored, only salt + hash. */
object PinSecurity {
    const val PIN_LENGTH = 6
    private const val ITERATIONS = 100_000
    private const val KEY_LENGTH = 256
    private val random = SecureRandom()

    fun isValidPin(pin: String): Boolean =
        pin.length == PIN_LENGTH && pin.all { it in '0'..'9' }

    fun generateSalt(): String {
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt.toHex()
    }

    fun hashPin(pin: String, saltHex: String): String {
        val spec = PBEKeySpec(pin.toCharArray(), saltHex.hexToBytes(), ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded.toHex()
    }

    fun verifyPin(pin: String, saltHex: String, expectedHashHex: String): Boolean {
        if (saltHex.isEmpty() || expectedHashHex.isEmpty()) return false
        val computed = hashPin(pin, saltHex).toByteArray()
        return MessageDigest.isEqual(computed, expectedHashHex.toByteArray())
    }

    private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }
    private fun String.hexToBytes() = chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
