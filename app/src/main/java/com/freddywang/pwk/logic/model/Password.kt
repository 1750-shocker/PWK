package com.freddywang.pwk.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    indices = [androidx.room.Index(name = "index_Password_des", value = ["des"])]
)
data class Password(
    val des: String, 
    val account: String, 
    val password: String,  // 存储加密后的密码
    val isEncrypted: Boolean  // 标记密码是否已加密，不要设置默认值
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
