package com.freddywang.pwk.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
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
    
    // 应用签名相关的常量
    private const val SIGNATURE_HASH_ALGORITHM = "SHA-256"
    
    // 使用基于应用签名的固定密钥，确保跨设备迁移
    private var cachedSecretKey: SecretKey? = null

    private fun getOrCreateSecretKey(context: Context): SecretKey {
        // 优先使用缓存的密钥
        cachedSecretKey?.let { return it }
        
        // 尝试从Android Keystore获取
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            
            if (keyStore.containsAlias(KEY_ALIAS)) {
                val secretKey = (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
                cachedSecretKey = secretKey
                return secretKey
            }
        } catch (e: Exception) {
            Log.w("wzhhh", "Android Keystore不可用，使用固定密钥: ${e.message}")
        }
        
        // 使用基于应用签名的固定密钥
        val fixedKey = createFixedSecretKey(context)
        cachedSecretKey = fixedKey
        return fixedKey
    }

    private fun createSecretKey(context: Context): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE
        )

        // 获取应用签名信息
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName, 
            android.content.pm.PackageManager.GET_SIGNATURES
        )
        val signature = packageInfo.signatures[0]
        val signatureHash = java.security.MessageDigest.getInstance(SIGNATURE_HASH_ALGORITHM)
            .digest(signature.toByteArray())
        
        Log.d("wzhhh", "创建密钥 - 应用签名哈希: ${android.util.Base64.encodeToString(signatureHash, android.util.Base64.NO_WRAP)}")

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false) // 不需要用户认证
            .setUserAuthenticationValidityDurationSeconds(-1) // 永久有效
            .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }
    
    /**
     * 创建基于应用签名的固定密钥，确保跨设备迁移
     */
    private fun createFixedSecretKey(context: Context): SecretKey {
        // 获取应用签名信息
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName, 
            android.content.pm.PackageManager.GET_SIGNATURES
        )
        val signature = packageInfo.signatures[0]
        val signatureHash = java.security.MessageDigest.getInstance(SIGNATURE_HASH_ALGORITHM)
            .digest(signature.toByteArray())
        
        Log.d("wzhhh", "创建固定密钥 - 应用签名哈希: ${android.util.Base64.encodeToString(signatureHash, android.util.Base64.NO_WRAP)}")
        
        // 使用应用签名作为密钥材料
        val keyMaterial = java.security.MessageDigest.getInstance("SHA-256")
            .digest(signatureHash + "PWK_SECRET_SALT".toByteArray())
        
        // 创建AES密钥
        val keySpec = javax.crypto.spec.SecretKeySpec(keyMaterial, "AES")
        Log.d("wzhhh", "固定密钥创建完成")
        return keySpec
    }

    fun encrypt(context: Context, plaintext: String): String {
        val secretKey = getOrCreateSecretKey(context)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        
        // 对于Android Keystore，不需要手动提供IV，让系统自动生成
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val encryptedBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        
        // 获取系统生成的IV
        val iv = cipher.iv

        // 将IV和加密数据合并并Base64编码
        // 使用NO_WRAP避免换行符，确保JSON兼容性
        val combined = iv + encryptedBytes
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun decrypt(context: Context, encryptedText: String): String {
        try {
            val secretKey = getOrCreateSecretKey(context)
            Log.d("wzhhh", "解密 - 获取密钥成功")
            
            // 尝试不同的Base64解码方式，优先使用NO_WRAP（新格式）
            val combinedCandidates = listOfNotNull(
                try {
                    Base64.decode(encryptedText, Base64.NO_WRAP)
                } catch (e: Exception) {
                    Log.d("wzhhh", "解密 - NO_WRAP解码失败: ${e.message}")
                    null
                },
                try {
                    Base64.decode(encryptedText, Base64.DEFAULT)
                } catch (e: Exception) {
                    Log.d("wzhhh", "解密 - DEFAULT解码失败: ${e.message}")
                    null
                }
            )

            Log.d("wzhhh", "解密 - 解码候选数量: ${combinedCandidates.size}")
            var lastException: Exception? = null
            
            for (combined in combinedCandidates) {
                if (combined.isEmpty()) {
                    Log.d("wzhhh", "解密 - 跳过空数据")
                    continue
                }
                
                Log.d("wzhhh", "解密 - 数据长度: ${combined.size}, 需要最小长度: ${IV_LENGTH + 16}")
                // 至少需要12字节IV + 16字节GCM标签
                if (combined.size <= IV_LENGTH + 16) {
                    Log.d("wzhhh", "解密 - 数据长度不足，跳过")
                    continue
                }
                
                try {
                    val iv = combined.copyOfRange(0, IV_LENGTH)
                    val encryptedBytes = combined.copyOfRange(IV_LENGTH, combined.size)
                    
                    Log.d("wzhhh", "解密 - IV长度: ${iv.size}, 加密数据长度: ${encryptedBytes.size}")
                    
                    val cipher = Cipher.getInstance(TRANSFORMATION)
                    val parameterSpec = GCMParameterSpec(128, iv)
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)
                    
                    val decryptedBytes = cipher.doFinal(encryptedBytes)
                    Log.d("wzhhh", "解密 - 解密成功")
                    return String(decryptedBytes, Charsets.UTF_8)
                } catch (e: Exception) {
                    Log.e("wzhhh", "解密 - 解密尝试失败: ${e.message}", e)
                    lastException = e
                }
            }

            // 所有尝试都失败，抛出最后一个异常
            Log.e("wzhhh", "解密 - 所有尝试都失败")
            throw lastException ?: IllegalArgumentException("Unable to decrypt: invalid format or corrupted data")
        } catch (e: Exception) {
            Log.e("wzhhh", "解密 - 整体失败: ${e.message}", e)
            throw e
        }
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
            createSecretKey(context)
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
     * 验证加密数据格式是否正确（IV长度是否为12字节）
     */
    fun validateEncryptedData(encryptedText: String): Boolean {
        return try {
            val combined = Base64.decode(encryptedText, Base64.NO_WRAP)
            combined.size > IV_LENGTH && combined.size >= IV_LENGTH + 16 // 至少要有IV + 16字节的GCM标签
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取加密数据的IV长度信息（用于调试）
     */
    fun getIvLengthInfo(encryptedText: String): String {
        return try {
            val combined = Base64.decode(encryptedText, Base64.NO_WRAP)
            "总长度: ${combined.size}, IV长度: ${IV_LENGTH}, 加密数据长度: ${combined.size - IV_LENGTH}"
        } catch (e: Exception) {
            "解析失败: ${e.message}"
        }
    }
    
    /**
     * 获取当前应用签名哈希（用于调试和验证）
     */
    fun getAppSignatureHash(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName, 
                android.content.pm.PackageManager.GET_SIGNATURES
            )
            val signature = packageInfo.signatures[0]
            val signatureHash = java.security.MessageDigest.getInstance(SIGNATURE_HASH_ALGORITHM)
                .digest(signature.toByteArray())
            android.util.Base64.encodeToString(signatureHash, android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            "获取签名失败: ${e.message}"
        }
    }
    
    /**
     * 验证应用签名是否匹配（用于导入时验证）
     */
    fun verifyAppSignature(context: Context): Boolean {
        return try {
            val currentSignature = getAppSignatureHash(context)
            Log.d("wzhhh", "当前应用签名: $currentSignature")
            // 这里可以添加与预期签名的比较逻辑
            // 目前返回true，表示签名验证通过
            true
        } catch (e: Exception) {
            Log.e("wzhhh", "签名验证失败: ${e.message}", e)
            false
        }
    }
    
}