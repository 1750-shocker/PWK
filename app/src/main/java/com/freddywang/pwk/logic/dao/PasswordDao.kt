package com.freddywang.pwk.logic.dao

import androidx.room.*
import com.freddywang.pwk.logic.model.Password

@Dao
interface PasswordDao {
    @Insert
    fun insertPassword(password: Password): Long

    @Update
    fun updatePassword(password: Password)

    @Query("select * from Password")
    fun outPutAllPassword(): List<Password>?

     @Query("select * from Password where des like '%' || :keyword || '%'")
    fun queryWithKeyWord(keyword: String): List<Password>

    @Delete
    fun deletePassword(password: Password)

    @Query("delete from Password")
    fun cleanTable()
}