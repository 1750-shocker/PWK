package com.freddywang.pwk.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    indices = [androidx.room.Index(name = "index_Password_des", value = ["des"])]
)
data class Password(
    val des: String, 
    val account: String, 
    val password: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
