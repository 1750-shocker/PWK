package com.freddywang.pwk.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Password(val des: String, val account: String, val password: String) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
