package com.freddywang.pwk.ui.edit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.freddywang.pwk.logic.AppDatabase
import com.freddywang.pwk.logic.model.Password

class EditViewModel(application: Application) : AndroidViewModel(application) {
    private var myDatabase: AppDatabase

    init {
        myDatabase = AppDatabase.getDatabase(application)
    }

    fun addPw(password: Password): Long {
        return myDatabase.passwordDao().insertPassword(password)
    }

    fun updatePw(password: Password) {
        myDatabase.passwordDao().updatePassword(password)
    }
}