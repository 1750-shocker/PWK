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
        private const val TAG = "MainViewModel"
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
            Log.d(TAG, "数据库中没有数据")
            cachedPasswords = arrayListOf()
            return cachedPasswords
        }
        
        val decryptedList = mutableListOf<Password>()
        
        encryptedList.forEach { password ->
            // 使用tryDecrypt避免抛异常，更优雅地处理解密失败
            val decryptedPasswordText = CryptoUtil.tryDecrypt(getApplication(), password.password)
            
            if (decryptedPasswordText != null) {
                // 解密成功
                val decryptedPassword = Password(
                    des = password.des,
                    account = password.account,
                    password = decryptedPasswordText,
                    isEncrypted = false // 标记为已解密状态，用于UI显示
                ).apply { id = password.id }
                decryptedList.add(decryptedPassword)
                Log.d(TAG, "成功解密密码项: id=${password.id}, des=${password.des}")
            } else {
                // 解密失败，可能是不兼容的加密数据
                Log.e(TAG, "解密密码失败: id=${password.id}, des=${password.des} - 可能是不兼容的加密格式")
                val errorPassword = Password(
                    des = password.des + " (解密失败)",
                    account = password.account,
                    password = "数据不兼容，需要重置",
                    isEncrypted = false
                ).apply { id = password.id }
                decryptedList.add(errorPassword)
            }
        }
        
        cachedPasswords = ArrayList(decryptedList)
        Log.d(TAG, "总共加载${encryptedList.size}个密码项，成功${decryptedList.size}个")
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
        return myDatabase.passwordDao().insertPassword(encryptedPassword)
    }

    fun addPw2(password: Password): Long {
        clearCache() // 添加新数据时清除缓存
        // 导入时直接使用JSON中的数据，因为导出时已经是加密状态
        // 不需要重复加密，直接存储即可
        return myDatabase.passwordDao().insertPassword(password)
    }

    fun getDecryptedPassword(context: Context, encryptedPassword: Password): Password {
        return if (encryptedPassword.isEncrypted) {
            val decryptedText = CryptoUtil.tryDecrypt(context, encryptedPassword.password)
            if (decryptedText != null) {
                Password(
                    des = encryptedPassword.des,
                    account = encryptedPassword.account,
                    password = decryptedText,
                    isEncrypted = false
                )
            } else {
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
            if (CryptoUtil.tryDecrypt(getApplication(), password.password) == null) {
                Log.w(TAG, "删除损坏的密码数据: id=${password.id}, des=${password.des}")
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
    
    /**
     * 测试JSON导入导出是否会损坏加密数据
     */
    fun testJsonIntegrity(): Boolean {
        return try {
            // 创建测试密码
            val testPassword = Password("测试", "testuser", "testpass123", false)
            val encryptedPassword = Password(
                testPassword.des,
                testPassword.account, 
                CryptoUtil.encrypt(getApplication(), testPassword.password),
                true
            )
            
            // 测试JSON序列化和反序列化
            val testList = listOf(encryptedPassword)
            val json = com.freddywang.pwk.logic.listToJson(testList)
            Log.d(TAG, "JSON导出内容: $json")
            
            val importedList = com.freddywang.pwk.logic.jsonToList(json)
            val importedPassword = importedList[0]
            
            // 尝试解密导入的数据
            val decryptedText = CryptoUtil.tryDecrypt(getApplication(), importedPassword.password)
            val success = decryptedText == testPassword.password
            
            Log.i(TAG, "JSON完整性测试结果: ${if (success) "成功" else "失败"}")
            if (!success) {
                Log.e(TAG, "原始密码: ${testPassword.password}")
                Log.e(TAG, "加密后: ${encryptedPassword.password}")
                Log.e(TAG, "JSON导入后: ${importedPassword.password}")
                Log.e(TAG, "解密结果: $decryptedText")
            }
            
            success
        } catch (e: Exception) {
            Log.e(TAG, "JSON完整性测试出错", e)
            false
        }
    }
}