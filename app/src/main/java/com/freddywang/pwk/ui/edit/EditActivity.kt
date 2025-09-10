package com.freddywang.pwk.ui.edit

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.freddywang.pwk.R
import com.freddywang.pwk.logic.model.Password
import com.freddywang.pwk.util.CryptoUtil
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText

class EditActivity : AppCompatActivity() {
    private val topAppBar: MaterialToolbar by lazy { findViewById<MaterialToolbar>(R.id.topAppBar) }
    private val textFieldDes: TextInputEditText by lazy { findViewById<TextInputEditText>(R.id.textField_des) }
    private val textFieldAccount: TextInputEditText by lazy { findViewById<TextInputEditText>(R.id.textField_account) }
    private val textFieldPassword: TextInputEditText by lazy { findViewById<TextInputEditText>(R.id.textField_password) }
    private var way: Int = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        way = intent.getIntExtra("way", 1)
        var id: Long = 0
        if (way == 2) {
            val bundle = intent.extras
            if (bundle != null) {
                id = bundle["id"] as Long
                textFieldDes.text = SpannableStringBuilder(bundle["des"].toString())
                textFieldAccount.text = SpannableStringBuilder(bundle["account"].toString())
                
                // 检查密码是否加密并解密显示
                val passwordFromBundle = bundle["password"].toString()
                val decryptedPassword = if (passwordFromBundle.startsWith("encrypted:")) {
                    try {
                        CryptoUtil.decrypt(this, passwordFromBundle.substring(10))
                    } catch (e: Exception) {
                        passwordFromBundle
                    }
                } else {
                    passwordFromBundle
                }
                textFieldPassword.text = SpannableStringBuilder(decryptedPassword)
            }
        }
        
        // 设置工具栏菜单项颜色
        setupToolbarMenuColors()
        
        val viewModel = ViewModelProvider(this).get(EditViewModel::class.java)
        topAppBar.setNavigationOnClickListener {
            if (way == 2) {
                Toast.makeText(this, "已取消", Toast.LENGTH_SHORT).show()
            }
            finish()
        }

        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.save -> {
                    // Handle favorite icon press
                    if (textFieldDes.editableText.isEmpty()) {
                        Toast.makeText(this, "请输入描述", Toast.LENGTH_SHORT).show()
                    } else if (textFieldAccount.editableText.isEmpty()) {
                        Toast.makeText(this, "请输入账号", Toast.LENGTH_SHORT).show()
                    } else if (textFieldPassword.editableText.isEmpty()) {
                        Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show()
                    } else if (way == 2) {
                        // 加密密码
                        val encryptedPassword = CryptoUtil.encrypt(this, textFieldPassword.text.toString())
                        val p = Password(
                            textFieldDes.text.toString(),
                            textFieldAccount.text.toString(),
                            encryptedPassword,
                            isEncrypted = true
                        )
                        p.id = id
                        viewModel.updatePw(p)
                        Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show()
                        finish()
                    } else if (way == 1) {
                        // 加密密码
                        val encryptedPassword = CryptoUtil.encrypt(this, textFieldPassword.editableText.toString())
                        val result = viewModel.addPw(
                            Password(
                                textFieldDes.editableText.toString(),
                                textFieldAccount.editableText.toString(),
                                encryptedPassword,
                                isEncrypted = true
                            )
                        )
                        if (result > 0) {
                            Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupToolbarMenuColors() {
        // 获取当前主题的颜色
        val typedArray = theme.obtainStyledAttributes(intArrayOf(
            com.google.android.material.R.attr.colorOnPrimary
        ))
        val textColor = typedArray.getColor(0, ContextCompat.getColor(this, android.R.color.white))
        typedArray.recycle()
        
        // 设置溢出菜单图标颜色
        topAppBar.overflowIcon?.setTint(textColor)
    }
}