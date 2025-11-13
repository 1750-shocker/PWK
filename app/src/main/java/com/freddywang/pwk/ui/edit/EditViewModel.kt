package com.freddywang.pwk.ui.edit

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.freddywang.pwk.logic.AppDatabase
import com.freddywang.pwk.logic.model.Password
 

class EditViewModel(application: Application) : AndroidViewModel(application) {
    private var myDatabase: AppDatabase = try {
        AppDatabase.getDatabase(application)
    } catch (e: Exception) {
        Log.e("wzhhh", "数据库初始化失败: ${e.message}", e)
        throw e
    }

    fun addPw(password: Password): Long {
        return try {
            Log.d("wzhhh", "开始添加密码: des=${password.des}, account=${password.account}")
            val plainPassword = Password(
                des = password.des,
                account = password.account,
                password = password.password
            )
            val result = myDatabase.passwordDao().insertPassword(plainPassword)
            Log.d("wzhhh", "密码插入数据库完成，返回ID: $result")
            result
        } catch (e: Exception) {
            Log.e("wzhhh", "添加密码失败: ${e.message}", e)
            -1
        }
    }

    fun updatePw(password: Password) {
        Log.d("wzhhh", "updatePw: ${password.password}")
        myDatabase.passwordDao().updatePassword(password)
    }
}