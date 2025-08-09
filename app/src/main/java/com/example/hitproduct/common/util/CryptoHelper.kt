package com.example.hitproduct.common.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * CryptoHelper tương thích Android 8+ với ECDH (secp256r1) + AES/GCM:
 * - Sinh cặp EC keypair P-256
 * - Trao đổi public key
 * - Derive shared AES key
 * - AES-GCM encrypt/decrypt
 * - Lưu/xóa keypair, peer public và shared key trong EncryptedSharedPreferences
 */
object CryptoHelper {
    private const val CRYPTO_PREFS_NAME = "crypto_prefs"
    private const val KEY_PRIV = "ecdh_priv"
    private const val KEY_PUB = "ecdh_pub"
    private const val KEY_PEER_PUB = "ecdh_peer_pub"
    private const val KEY_SHARED_AES = "ecdh_shared_aes"
    private const val KEY_PRIV_ENC = "ecdh_priv_enc"
    private const val SALT_LENGTH = 16
    private const val PIN_KDF_ALGO = "PBKDF2WithHmacSHA256"
    private const val PIN_KDF_ITERATIONS = 200_000
    private const val PIN_KEY_LENGTH = 256 // bits

    private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_LENGTH = 12
    private const val TAG_LENGTH_BITS = 128

    private const val EC_ALGORITHM = "EC"
    private const val EC_CURVE_NAME = "secp256r1"
    private const val KEY_AGREEMENT_ALGO = "ECDH"

    private const val MASTER_KEY_ALIAS = MasterKey.DEFAULT_MASTER_KEY_ALIAS

    private fun getPrefs(ctx: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(ctx, MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            ctx, CRYPTO_PREFS_NAME, masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /** Sinh EC keypair P-256 một lần */
    fun ensureKeyPair(ctx: Context) {
        val prefs = getPrefs(ctx)
        if (prefs.getString(KEY_PRIV, null) == null) {
            val kpg = KeyPairGenerator.getInstance(EC_ALGORITHM)
            kpg.initialize(ECGenParameterSpec(EC_CURVE_NAME))
            val pair = kpg.generateKeyPair()
            prefs.edit().apply {
                putString(KEY_PRIV, Base64.encodeToString(pair.private.encoded, Base64.NO_WRAP))
                putString(KEY_PUB, Base64.encodeToString(pair.public.encoded, Base64.NO_WRAP))
                apply()
            }
        }
    }

    /** Lấy public key Base64 */
    fun getMyPublicKey(ctx: Context): String =
        getPrefs(ctx).getString(KEY_PUB, "").orEmpty()

    /** Lưu public key peer */
    fun storePeerPublicKey(ctx: Context, peerPubB64: String) {
        getPrefs(ctx).edit().putString(KEY_PEER_PUB, peerPubB64).apply()
    }

    /** Lấy public key peer */
    fun getPeerPublicKey(ctx: Context): String? =
        getPrefs(ctx).getString(KEY_PEER_PUB, null)

    /** Derive và lưu shared AES key */
    fun deriveAndStoreSharedAesKey(ctx: Context) {
        val peerB64 = getPeerPublicKey(ctx)
            ?: error("Peer public key missing")
        // parse keys
        val prefs = getPrefs(ctx)
        val privBytes = Base64.decode(prefs.getString(KEY_PRIV, ""), Base64.NO_WRAP)
        val pubBytes = Base64.decode(peerB64, Base64.NO_WRAP)
        val kf = KeyFactory.getInstance(EC_ALGORITHM)
        val privateKey = kf.generatePrivate(PKCS8EncodedKeySpec(privBytes))
        val publicKey = kf.generatePublic(X509EncodedKeySpec(pubBytes))
        // agree
        val ka = KeyAgreement.getInstance(KEY_AGREEMENT_ALGO)
        ka.init(privateKey)
        ka.doPhase(publicKey, true)
        val sharedSecret = ka.generateSecret()
        val aesKeyBytes = MessageDigest.getInstance("SHA-256").digest(sharedSecret)
        val aesKey = SecretKeySpec(aesKeyBytes, "AES")
        prefs.edit().putString(KEY_SHARED_AES, Base64.encodeToString(aesKeyBytes, Base64.NO_WRAP))
            .apply()
    }

    /** Lấy AES key đã lưu */
    fun getSharedAesKey(ctx: Context): SecretKey? {
        val b64 = getPrefs(ctx).getString(KEY_SHARED_AES, null) ?: return null
        val bytes = Base64.decode(b64, Base64.NO_WRAP)
        return SecretKeySpec(bytes, "AES")
    }

    /** Mã hóa AES-GCM */
    fun encrypt(aesKey: SecretKey, plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        val iv = ByteArray(IV_LENGTH).apply { SecureRandom().nextBytes(this) }
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, GCMParameterSpec(TAG_LENGTH_BITS, iv))
        val ciphertext = cipher.doFinal(plaintext)
        return iv + ciphertext
    }

    /** Giải mã AES-GCM */
    fun decrypt(aesKey: SecretKey, data: ByteArray): ByteArray {
        val iv = data.copyOfRange(0, IV_LENGTH)
        val ct = data.copyOfRange(IV_LENGTH, data.size)
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, aesKey, GCMParameterSpec(TAG_LENGTH_BITS, iv))
        return cipher.doFinal(ct)
    }

    /** Xóa keypair, peer pub và shared AES */
    fun deleteAllKeys(ctx: Context) {
        getPrefs(ctx).edit().apply {
            remove(KEY_PRIV); remove(KEY_PUB)
            remove(KEY_PEER_PUB); remove(KEY_SHARED_AES)
            remove(KEY_PRIV_ENC)
            apply()
        }
    }


    /** Helper private: derive AES key từ PIN + salt bằng PBKDF2 */
    private fun deriveKeyFromPin(pin: CharArray, salt: ByteArray): SecretKey {
        val spec = javax.crypto.spec.PBEKeySpec(pin, salt, PIN_KDF_ITERATIONS, PIN_KEY_LENGTH)
        val factory = javax.crypto.SecretKeyFactory.getInstance(PIN_KDF_ALGO)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * Encrypt private key đã lưu (KEY_PRIV) bằng PIN 6 số,
     * lưu blob = salt || iv || ciphertext vào EncryptedSharedPreferences
     */
    fun encryptPrivateKeyWithPin(ctx: Context, pin: String) {
        val prefs = getPrefs(ctx)
        // 1. Lấy private key gốc (Base64)
        val privB64 = prefs.getString(KEY_PRIV, null)
            ?: error("Private key missing")
        val privBytes = Base64.decode(privB64, Base64.NO_WRAP)

        // 2. Sinh salt & derive AES key
        val salt = ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }
        val aesKey = deriveKeyFromPin(pin.toCharArray(), salt)

        // 3. Encrypt với AES/GCM
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        val iv = ByteArray(IV_LENGTH).also { SecureRandom().nextBytes(it) }
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, GCMParameterSpec(TAG_LENGTH_BITS, iv))
        val ciphertext = cipher.doFinal(privBytes)

        // 4. Gộp blob và lưu Base64
        val blob = salt + iv + ciphertext
        prefs.edit()
            .putString(KEY_PRIV_ENC, Base64.encodeToString(blob, Base64.NO_WRAP))
            .apply()
    }

    /**
     * Decrypt blob đã lưu (KEY_PRIV_ENC) với PIN,
     * trả về đúng byte[] của private key
     */
    fun decryptPrivateKeyWithPin(ctx: Context, pin: String): ByteArray {
        val prefs = getPrefs(ctx)
        val blobB64 = prefs.getString(KEY_PRIV_ENC, null)
            ?: error("Encrypted private key missing")
        val blob = Base64.decode(blobB64, Base64.NO_WRAP)

        // Tách salt, iv, ciphertext
        val salt = blob.copyOfRange(0, SALT_LENGTH)
        val iv = blob.copyOfRange(SALT_LENGTH, SALT_LENGTH + IV_LENGTH)
        val ct = blob.copyOfRange(SALT_LENGTH + IV_LENGTH, blob.size)

        // Derive lại key & decrypt
        val aesKey = deriveKeyFromPin(pin.toCharArray(), salt)
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, aesKey, GCMParameterSpec(TAG_LENGTH_BITS, iv))
        return cipher.doFinal(ct) // chính là privateKey bytes
    }

    /**
     * Lưu blob Base64 (salt‖iv‖ciphertext) mà bạn lấy về từ server
     */
    fun storeEncryptedPrivateKeyBlob(ctx: Context, blobB64: String) {
        getPrefs(ctx).edit()
            .putString(KEY_PRIV_ENC, blobB64)
            .apply()
    }

    /**
     * Lấy blob Base64 của private key đã mã hoá (ecdh_priv_enc) từ crypto_prefs
     */
    fun getEncryptedPrivateKeyB64(ctx: Context): String =
        getPrefs(ctx).getString(KEY_PRIV_ENC, "").orEmpty()

    /**
     * Restore raw private key (đã giải mã) vào EncryptedSharedPreferences
     * để dùng cho các thao tác ECDH tiếp theo.
     */
    fun restorePrivateKey(ctx: Context, rawPrivBytes: ByteArray) {
        // Mã hoá lại thành Base64
        val b64 = Base64.encodeToString(rawPrivBytes, Base64.NO_WRAP)
        // Lưu vào prefs dưới KEY_PRIV
        getPrefs(ctx).edit()
            .putString(KEY_PRIV, b64)
            .apply()
    }


    fun hasRawPrivateKey(ctx: Context): Boolean =
        getPrefs(ctx).contains(KEY_PRIV)

    fun clearPairingData(ctx: Context) {
        getPrefs(ctx).edit().apply {
            remove(KEY_PEER_PUB)
//            remove(KEY_SHARED_AES)
            apply()
        }
    }
}
