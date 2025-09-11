package com.freddywang.pwk.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoUtil {
    private const val KEY_ALIAS = "PWK_AES_KEY"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_LENGTH = 12 // GCM推荐12字节的IV

    private fun getOrCreateSecretKey(context: Context): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        return if (keyStore.containsAlias(KEY_ALIAS)) {
            (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        } else {
            createSecretKey()
        }
    }

    private fun createSecretKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE
        )

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    fun encrypt(context: Context, plaintext: String): String {
        val secretKey = getOrCreateSecretKey(context)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        // 将IV和加密数据合并并Base64编码
        // 使用NO_WRAP避免换行符，确保JSON兼容性
        val combined = iv + encryptedBytes
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun decrypt(context: Context, encryptedText: String): String {
        val secretKey = getOrCreateSecretKey(context)
        
        // 尝试不同的Base64解码方式，优先使用NO_WRAP（新格式）
        val combinedCandidates = listOf(
            try { Base64.decode(encryptedText, Base64.NO_WRAP) } catch (e: Exception) { null },
            try { Base64.decode(encryptedText, Base64.DEFAULT) } catch (e: Exception) { null }
        ).filterNotNull()

        var lastException: Exception? = null
        
        for (combined in combinedCandidates) {
            if (combined.isEmpty()) continue
            
            // 尝试常见的GCM IV长度：12字节优先，然后是16字节
            val possibleIvLengths = listOf(12, 16)
            
            for (ivLen in possibleIvLengths) {
                if (combined.size <= ivLen) continue
                
                try {
                    val iv = combined.copyOfRange(0, ivLen)
                    val encryptedBytes = combined.copyOfRange(ivLen, combined.size)
                    
                    val cipher = Cipher.getInstance(TRANSFORMATION)
                    val parameterSpec = GCMParameterSpec(128, iv)
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)
                    
                    val decryptedBytes = cipher.doFinal(encryptedBytes)
                    return String(decryptedBytes, Charsets.UTF_8)
                } catch (e: Exception) {
                    lastException = e
                    // 如果是不支持的IV长度错误，直接跳过这个长度
                    if (e is java.security.InvalidAlgorithmParameterException && 
                        e.message?.contains("Unsupported IV length") == true) {
                        continue
                    }
                    // 继续尝试下一个IV长度
                }
            }
        }

        // 所有尝试都失败，抛出最后一个异常
        throw lastException ?: IllegalArgumentException("Unable to decrypt: invalid format or corrupted data")
    }

    // 检查密钥是否可用（用于测试）
    fun isKeyAvailable(context: Context): Boolean {
        return try {
            getOrCreateSecretKey(context)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 重置加密密钥（谨慎使用：会导致现有加密数据无法解密）
     */
    fun resetKey(context: Context): Boolean {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            if (keyStore.containsAlias(KEY_ALIAS)) {
                keyStore.deleteEntry(KEY_ALIAS)
            }
            createSecretKey()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 测试解密功能是否正常
     */
    fun testEncryptionDecryption(context: Context): Boolean {
        return try {
            val testData = "test"
            val encrypted = encrypt(context, testData)
            val decrypted = decrypt(context, encrypted)
            testData == decrypted
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 尝试解密，如果失败返回null而不抛异常
     * 用于检测损坏或不兼容的加密数据
     */
    fun tryDecrypt(context: Context, encryptedText: String): String? {
        return try {
            decrypt(context, encryptedText)
        } catch (e: Exception) {
            null
        }
    }
}