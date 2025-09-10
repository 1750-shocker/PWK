package com.freddywang.pwk.logic

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.freddywang.pwk.logic.dao.PasswordDao
import com.freddywang.pwk.logic.model.Password
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Database(version = 2, entities = [Password::class], exportSchema = false)
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
                .addMigrations(MIGRATION_1_2)
                .build().apply { instance = this }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. 创建临时表（完全匹配Room期望的结构）
                database.execSQL(
                    "CREATE TABLE Password_temp (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "des TEXT NOT NULL, " +
                    "account TEXT NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "isEncrypted INTEGER NOT NULL)"
                )
                
                // 2. 复制数据
                database.execSQL(
                    "INSERT INTO Password_temp (id, des, account, password, isEncrypted) " +
                    "SELECT id, des, account, password, 1 FROM Password"
                )
                
                // 3. 删除旧表
                database.execSQL("DROP TABLE Password")
                
                // 4. 重命名临时表
                database.execSQL("ALTER TABLE Password_temp RENAME TO Password")
                
                // 5. 创建索引（完全匹配Room期望的索引格式）
                database.execSQL("CREATE INDEX index_Password_des ON Password (des)")
            }
        }
    }
}

fun jsonToList(json: String): List<Password> {
    val type = object : TypeToken<List<Password>>() {}.type
    val passwords = Gson().fromJson<List<Password>>(json, type)
    // 确保导入的密码标记为未加密（将在保存时重新加密）
    return passwords.map { password ->
        if (password.isEncrypted) {
            Password(
                des = password.des,
                account = password.account,
                password = password.password,
                isEncrypted = false  // 标记为未加密，将在保存时重新加密
            )
        } else {
            password
        }
    }
}

fun listToJson(list: List<Password>): String {
    return Gson().toJson(list)
}
