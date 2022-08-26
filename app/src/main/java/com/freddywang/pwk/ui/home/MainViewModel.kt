package com.freddywang.pwk.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.freddywang.pwk.logic.AppDatabase
import com.freddywang.pwk.logic.model.Password

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private var myDatabase: AppDatabase

    init {
        myDatabase = AppDatabase.getDatabase(application)
    }


    fun outPutAllPassword(): ArrayList<Password>? {
        return myDatabase.passwordDao().outPutAllPassword() as ArrayList<Password>? /* = java.util.ArrayList<com.freddywang.pwk.logic.model.Password> */
    }

    fun queryWithKeyWord(keyword: String): ArrayList<Password> {
        return myDatabase.passwordDao().queryWithKeyWord(keyword) as ArrayList<Password> /* = java.util.ArrayList<com.freddywang.pwk.logic.model.Password> */
    }

    fun deletePw(password: Password) {
        myDatabase.passwordDao().deletePassword(password)
    }

    fun cleanTable(){
        myDatabase.passwordDao().cleanTable()
    }

    fun addPw(password: Password): Long {
        return myDatabase.passwordDao().insertPassword(password)
    }
}