// CryptoHelper.kt
package com.example.hitproduct.utils

import android.util.Base64
import java.nio.charset.Charset
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Helper object for AES/GCM encryption & decryption of String messages.
 * Use a 128- or 256-bit secret key. IV is randomly generated per message.
 */
object CryptoHelper {
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val KEY_ALGORITHM = "AES"
    private const val TAG_LENGTH_BIT = 128 // authentication tag length
    private const val IV_LENGTH_BYTE = 12 // recommended IV length for GCM

    /**
     * Create a SecretKey from a raw byte array. Ensure keyBytes length is 16 (128-bit)
     * or 32 (256-bit) depending on your target.
     */
    fun getSecretKey(keyBytes: ByteArray): SecretKey {
        require(keyBytes.size == 16 || keyBytes.size == 32) {
            "Key must be 128 or 256 bits (16 or 32 bytes)"
        }
        return SecretKeySpec(keyBytes, KEY_ALGORITHM)
    }

    /**
     * Encrypt plaintext (UTF-8) to Base64( IV + ciphertext ).
     */
    fun encrypt(plainText: String, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        // Generate random IV
        val iv = ByteArray(IV_LENGTH_BYTE)
        SecureRandom().nextBytes(iv)
        val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

        val cipherText = cipher.doFinal(plainText.toByteArray(Charset.forName("UTF-8")))
        // Prepend IV to cipherText
        val ivAndCipher = iv + cipherText
        // Return as Base64 string
        return Base64.encodeToString(ivAndCipher, Base64.NO_WRAP)
    }

    /**
     * Decrypt Base64( IV + ciphertext ) back to plaintext.
     */
    fun decrypt(base64IvAndCipher: String, secretKey: SecretKey): String {
        val ivAndCipher = Base64.decode(base64IvAndCipher, Base64.NO_WRAP)
        require(ivAndCipher.size > IV_LENGTH_BYTE) { "Invalid input data" }
        val iv = ivAndCipher.copyOfRange(0, IV_LENGTH_BYTE)
        val cipherBytes = ivAndCipher.copyOfRange(IV_LENGTH_BYTE, ivAndCipher.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        val plainBytes = cipher.doFinal(cipherBytes)
        return String(plainBytes, Charset.forName("UTF-8"))
    }
}
