package com.freddywang.pwk.ui.edit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.freddywang.pwk.logic.AppDatabase
import com.freddywang.pwk.logic.model.Password
import com.freddywang.pwk.util.CryptoUtil

class EditViewModel(application: Application) : AndroidViewModel(application) {
    private var myDatabase: AppDatabase = AppDatabase.getDatabase(application)

    fun addPw(password: Password): Long {
        // 确保密码被加密存储
        val encryptedPassword = Password(
            des = password.des,
            account = password.account,
            password = CryptoUtil.encrypt(getApplication(), password.password),
            isEncrypted = true
        )
        return myDatabase.passwordDao().insertPassword(encryptedPassword)
    }

    fun updatePw(password: Password) {
        myDatabase.passwordDao().updatePassword(password)
    }
}