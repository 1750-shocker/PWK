package com.freddywang.pwk.ui.home

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.freddywang.pwk.logic.AppDatabase
import com.freddywang.pwk.logic.model.Password
 

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
        val list = myDatabase.passwordDao().outPutAllPassword() as ArrayList<Password>?
        cachedPasswords = list ?: arrayListOf()
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
        clearCache()
        val plainPassword = Password(
            des = password.des,
            account = password.account,
            password = password.password
        )
        return myDatabase.passwordDao().insertPassword(plainPassword)
    }

    fun addPw2(password: Password): Long {
        clearCache()
        Log.d("wzhhh", "addPw2 - 导入密码: ${password.password}")
        return try {
            val plainPassword = Password(
                des = password.des,
                account = password.account,
                password = password.password
            )
            myDatabase.passwordDao().insertPassword(plainPassword)
        } catch (e: Exception) {
            Log.e("wzhhh", "addPw2 - 导入失败: ${e.message}", e)
            -1
        }
    }

    fun getDecryptedPassword(context: Context, encryptedPassword: Password): Password {
        return encryptedPassword
    }
}