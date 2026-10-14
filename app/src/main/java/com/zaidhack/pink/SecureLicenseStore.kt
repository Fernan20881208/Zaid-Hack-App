package com.zaidhack.pink

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Keeps the ZH license encrypted with a non-exportable key in AndroidKeyStore.
 * Migrates the old plaintext SharedPreferences entry on first access.
 */
object SecureLicenseStore {
    private const val ALIAS = "zh_license_aes_gcm_v1"
    private const val PREFERENCES = "zh_local"
    private const val ENCRYPTED = "license_ciphertext"

    private fun secret(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    fun save(context: Context, key: String) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secret())
        val ciphertext = cipher.doFinal(key.toByteArray(Charsets.UTF_8))
        val iv = cipher.iv
        val joined = iv + ciphertext
        val encoded = Base64.encodeToString(joined, Base64.NO_WRAP)
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit()
            .putString(ENCRYPTED, encoded).remove("key").apply()
    }

    fun read(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        val encoded = prefs.getString(ENCRYPTED, null)
        if (encoded != null) {
            return try {
                val raw = Base64.decode(encoded, Base64.NO_WRAP)
                if (raw.size < 12 + 16) return null
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.DECRYPT_MODE, secret(), GCMParameterSpec(128, raw.copyOfRange(0, 12)))
                String(cipher.doFinal(raw.copyOfRange(12, raw.size)), Charsets.UTF_8)
            } catch (_: Exception) {
                null
            }
        }
        val legacy = prefs.getString("key", null) ?: return null
        return try {
            save(context, legacy)
            legacy
        } catch (_: Exception) {
            null
        }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit()
            .remove(ENCRYPTED).remove("key").remove("expiry").apply()
    }
}
