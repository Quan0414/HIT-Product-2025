package com.example.hitproduct.common.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.hitproduct.common.constants.AuthPrefersConstants
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
    private const val PREFS_NAME = AuthPrefersConstants.PREFS_NAME
    private const val KEY_PRIV = "ecdh_priv"
    private const val KEY_PUB = "ecdh_pub"
    private const val KEY_PEER_PUB = "ecdh_peer_pub"
    private const val KEY_SHARED_AES = "ecdh_shared_aes"

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
            ctx, PREFS_NAME, masterKey,
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
            apply()
        }
    }
}
