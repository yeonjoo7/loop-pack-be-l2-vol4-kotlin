package com.loopers.domain.user

import java.security.MessageDigest

object PasswordEncryptor {
    fun encrypt(rawPassword: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(rawPassword.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun matches(rawPassword: String, encryptedPassword: String): Boolean {
        return encrypt(rawPassword) == encryptedPassword
    }
}
