package com.freddywang.pwk.ui.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.freddywang.pwk.logic.AppDatabase
import com.freddywang.pwk.logic.model.Password
import com.freddywang.pwk.util.CryptoUtil

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private var myDatabase: AppDatabase
    private var cachedPasswords: ArrayList<Password>? = null

    init {
        myDatabase = AppDatabase.getDatabase(application)
    }


    fun outPutAllPassword(): ArrayList<Password>? {
        if (cachedPasswords == null) {
            cachedPasswords = myDatabase.passwordDao().outPutAllPassword() as ArrayList<Password>?
        }
        return cachedPasswords
    }

    fun queryWithKeyWord(keyword: String): ArrayList<Password> {
        // 使用内存缓存进行搜索
        val allPasswords = outPutAllPassword()
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

    fun cleanTable(){
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

    fun getDecryptedPassword(context: Context, encryptedPassword: Password): Password {
        return if (encryptedPassword.isEncrypted) {
            Password(
                des = encryptedPassword.des,
                account = encryptedPassword.account,
                password = CryptoUtil.decrypt(context, encryptedPassword.password),
                isEncrypted = false
            )
        } else {
            encryptedPassword
        }
    }
}