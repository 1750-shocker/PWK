package com.freddywang.pwk.logic

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.freddywang.pwk.logic.dao.PasswordDao
import com.freddywang.pwk.logic.model.Password
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Database(version = 1, entities = [Password::class])
abstract class AppDatabase : RoomDatabase() {
    abstract fun passwordDao(): PasswordDao

    companion object {
        private var instance: AppDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): AppDatabase {
            instance?.let { return it }
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            ).allowMainThreadQueries()
                .build().apply { instance = this }
        }
    }
}

fun jsonToList(json: String): List<Password> {
    val type = object : TypeToken<List<Password>>() {}.type
    return Gson().fromJson(json, type)
}

fun listToJson(list: List<Password>): String {
    return Gson().toJson(list)
}

/*
fun hideInputMethod(act: Activity, v: View) {
    val imm = act.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(v.windowToken, 0)
}*/
