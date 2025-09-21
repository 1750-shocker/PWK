package com.freddywang.pwk.ui.home

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.freddywang.pwk.logic.AppDatabase
import com.freddywang.pwk.logic.model.Password
import com.freddywang.pwk.util.CryptoUtil

class MainViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "wzhhh"
    }
    
    private var myDatabase: AppDatabase = AppDatabase.getDatabase(application)
    private var cachedPasswords: ArrayList<Password>? = null


    fun outPutAllDBPassword(): ArrayList<Password>? {
        return myDatabase.passwordDao().outPutAllPassword() as ArrayList<Password>?
    }

    fun outPutAllUIPassword(): ArrayList<Password>? {
        // 在onResume时总是从数据库重新读取，确保数据是最新的
        val encryptedList = myDatabase.passwordDao().outPutAllPassword() as ArrayList<Password>?
        
        if (encryptedList.isNullOrEmpty()) {
            cachedPasswords = arrayListOf()
            return cachedPasswords
        }
        
        val decryptedList = mutableListOf<Password>()
        
        encryptedList.forEach { password ->
            if (password.isEncrypted) {
                try {
                    // 需要解密
                    val decryptedPasswordText = CryptoUtil.decrypt(getApplication(), password.password)
                    
                    // 解密成功
                    val decryptedPassword = Password(
                        des = password.des,
                        account = password.account,
                        password = decryptedPasswordText,
                        isEncrypted = false // 标记为已解密状态，用于UI显示
                    ).apply { id = password.id }
                    decryptedList.add(decryptedPassword)
                } catch (e: Exception) {
                    // 解密失败，记录错误日志但不崩溃
                    Log.e(TAG, "解密密码失败: id=${password.id}, des=${password.des} - ${e.message}")
                    val errorPassword = Password(
                        des = password.des + " (解密失败)",
                        account = password.account,
                        password = "数据不兼容，需要重置",
                        isEncrypted = false
                    ).apply { id = password.id }
                    decryptedList.add(errorPassword)
                }
            } else {
                // 已经是明文，直接使用
                val decryptedPassword = Password(
                    des = password.des,
                    account = password.account,
                    password = password.password, // 直接使用明文
                    isEncrypted = false
                ).apply { id = password.id }
                decryptedList.add(decryptedPassword)
            }
        }
        
        cachedPasswords = ArrayList(decryptedList)
        return cachedPasswords
    }

    fun queryWithKeyWord(keyword: String): ArrayList<Password> {
        // 使用内存缓存进行搜索
        val allPasswords = outPutAllUIPassword()
        if (allPasswords != null) {
            val result = ArrayList<Password>()
            for (password in allPasswords) {
                if (password.des.contains(keyword, ignoreCase = true)) {
                    result.add(password)
                }
            }
            return result
        }
        // 如果缓存为空，回退到数据库查询
        return myDatabase.passwordDao().queryWithKeyWord(keyword) as ArrayList<Password>
    }

    fun clearCache() {
        cachedPasswords = null
    }

    fun deletePw(password: Password) {
        myDatabase.passwordDao().deletePassword(password)
        clearCache() // 删除数据后清除缓存
    }

    fun cleanTable() {
        myDatabase.passwordDao().cleanTable()
        clearCache() // 清空表后清除缓存
    }

    fun addPw(password: Password): Long {
        clearCache() // 添加新数据时清除缓存
        // 无论标记如何，都进行实际加密以确保数据安全
        val encryptedPassword = Password(
            des = password.des,
            account = password.account,
            password = CryptoUtil.encrypt(getApplication(), password.password),
            isEncrypted = true
        )
        Log.d("wzhhh", "addPw - 原始密码: ${password.password}, 加密后: ${encryptedPassword.password}, isEncrypted: ${encryptedPassword.isEncrypted}")
        return myDatabase.passwordDao().insertPassword(encryptedPassword)
    }

    fun addPw2(password: Password): Long {
        clearCache() // 添加新数据时清除缓存
        Log.d("wzhhh", "addPw2 - 导入密码: ${password.password}, isEncrypted: ${password.isEncrypted}")
        
        return try {
            if (password.isEncrypted) {
                // 尝试解密，测试当前密钥是否匹配
                try {
                    val testDecrypt = CryptoUtil.decrypt(getApplication(), password.password)
                    Log.d("wzhhh", "addPw2 - 密钥匹配，直接存储")
                    // 密钥匹配，直接存储
                    myDatabase.passwordDao().insertPassword(password)
                } catch (e: Exception) {
                    Log.e("wzhhh", "addPw2 - 密钥不匹配，重新加密: ${e.message}")
                    // 密钥不匹配，需要重新加密
                    // 由于无法解密，我们无法获取明文密码
                    // 这种情况下，我们只能存储加密失败的状态
                    val failedPassword = Password(
                        des = password.des,
                        account = password.account,
                        password = "密钥不匹配，无法解密",
                        isEncrypted = false
                    )
                    myDatabase.passwordDao().insertPassword(failedPassword)
                }
            } else {
                // 明文密码，直接加密存储
                val encryptedPassword = Password(
                    des = password.des,
                    account = password.account,
                    password = CryptoUtil.encrypt(getApplication(), password.password),
                    isEncrypted = true
                )
                myDatabase.passwordDao().insertPassword(encryptedPassword)
            }
        } catch (e: Exception) {
            Log.e("wzhhh", "addPw2 - 导入失败: ${e.message}", e)
            -1
        }
    }

    fun getDecryptedPassword(context: Context, encryptedPassword: Password): Password {
        return if (encryptedPassword.isEncrypted) {
            try {
                val decryptedText = CryptoUtil.decrypt(context, encryptedPassword.password)
                Password(
                    des = encryptedPassword.des,
                    account = encryptedPassword.account,
                    password = decryptedText,
                    isEncrypted = false
                )
            } catch (e: Exception) {
                Log.e(TAG, "解密密码失败: ${e.message}", e)
                Log.e(TAG, "加密密码内容: ${encryptedPassword.password}")
                Log.e(TAG, "密码长度: ${encryptedPassword.password.length}")
                // 解密失败的情况
                Password(
                    des = encryptedPassword.des,
                    account = encryptedPassword.account,
                    password = "解密失败",
                    isEncrypted = false
                )
            }
        } else {
            encryptedPassword
        }
    }
    
    /**
     * 检查并清理损坏的加密数据
     * 谨慎使用：这会删除无法解密的数据
     */
    fun cleanCorruptedData(): Int {
        val allPasswords = myDatabase.passwordDao().outPutAllPassword()
        var deletedCount = 0
        
        allPasswords?.forEach { password ->
            try {
                CryptoUtil.decrypt(getApplication(), password.password)
            } catch (e: Exception) {
                Log.w(TAG, "删除损坏的密码数据: id=${password.id}, des=${password.des} - ${e.message}")
                myDatabase.passwordDao().deletePassword(password)
                deletedCount++
            }
        }
        
        clearCache()
        Log.i(TAG, "清理损坏数据完成，删除${deletedCount}条记录")
        return deletedCount
    }
    
    /**
     * 重置加密密钥并清理所有数据
     * 警告：这会删除所有现有密码数据！
     */
    fun resetEncryptionAndCleanData(): Boolean {
        return try {
            // 重置加密密钥
            if (CryptoUtil.resetKey(getApplication())) {
                // 清空所有数据
                cleanTable()
                Log.i(TAG, "成功重置加密密钥并清理数据")
                true
            } else {
                Log.e(TAG, "重置加密密钥失败")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "重置加密密钥过程出错", e)
            false
        }
    }
}