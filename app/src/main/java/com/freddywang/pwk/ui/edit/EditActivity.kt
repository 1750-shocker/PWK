package com.freddywang.pwk.ui.edit

import android.os.Bundle
import android.util.Log
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
 
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText

class EditActivity : AppCompatActivity() {
    companion object {
        const val MODE_ADD = 1
        const val MODE_EDIT = 2
        const val TAG = "wzhhh"
    }
    
    private val topAppBar: MaterialToolbar by lazy { findViewById<MaterialToolbar>(R.id.topAppBar) }
    private val textFieldDes: TextInputEditText by lazy { findViewById<TextInputEditText>(R.id.textField_des) }
    private val textFieldAccount: TextInputEditText by lazy { findViewById<TextInputEditText>(R.id.textField_account) }
    private val textFieldPassword: TextInputEditText by lazy { findViewById<TextInputEditText>(R.id.textField_password) }
    private var way: Int = MODE_ADD
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        way = intent.getIntExtra("way", MODE_ADD)
        var id: Long = 0
        if (way == MODE_EDIT) {
            val bundle = intent.extras
            if (bundle != null) {
                id = bundle["id"] as Long
                textFieldDes.text = SpannableStringBuilder(bundle["des"].toString())
                textFieldAccount.text = SpannableStringBuilder(bundle["account"].toString())
                textFieldPassword.text = SpannableStringBuilder(bundle["password"].toString())
            }
            Log.d(TAG, "编辑模式进来传id:${id}")
        }
        
        // 设置工具栏菜单项颜色
        setupToolbarMenuColors()
        
        val viewModel = ViewModelProvider(this).get(EditViewModel::class.java)
        topAppBar.setNavigationOnClickListener {
            if (way == MODE_EDIT) {
                Toast.makeText(this, "已取消", Toast.LENGTH_SHORT).show()
            }
            finish()
        }

        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.save -> {
                    Log.d(TAG, "点击保存按钮，当前模式: $way")
                    if (textFieldDes.text?.toString().isNullOrEmpty()) {
                        Toast.makeText(this, "请输入描述", Toast.LENGTH_SHORT).show()
                    } else if (textFieldAccount.text?.toString().isNullOrEmpty()) {
                        Toast.makeText(this, "请输入账号", Toast.LENGTH_SHORT).show()
                    } else if (textFieldPassword.text?.toString().isNullOrEmpty()) {
                        Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show()
                    } else if (way == MODE_EDIT) {
                        // 编辑模式：直接保存明文密码
                        val currentPassword = textFieldPassword.text.toString()
                        Log.d(TAG, "当前明文:${currentPassword}")
                        Log.d(TAG, "将要更新的id:${id}")
                        val p = Password(
                            textFieldDes.text.toString(),
                            textFieldAccount.text.toString(),
                            currentPassword
                        )
                        p.id = id
                        Log.d(TAG, "准备更新密码对象: id=${p.id}, des=${p.des}, account=${p.account} password=${p.password}")
                        viewModel.updatePw(p)
                        Log.d(TAG, "密码更新完成")
                        Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show()
                        finish()
                    } else if (way == MODE_ADD) {
                        // 新增模式：直接以明文交给 ViewModel 保存
                        Log.d(TAG, "开始新增密码数据")
                        val result = viewModel.addPw(
                            Password(
                                textFieldDes.text?.toString() ?: "",
                                textFieldAccount.text?.toString() ?: "",
                                textFieldPassword.text?.toString() ?: ""
                            )
                        )
                        Log.d(TAG, "新增密码结果: $result")
                        if (result > 0) {
                            Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Log.e(TAG, "新增密码失败，返回结果: $result")
                            Toast.makeText(this, "保存失败，请重试", Toast.LENGTH_SHORT).show()
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