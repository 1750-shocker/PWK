package com.freddywang.pwk.ui.edit

import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.freddywang.pwk.R
import com.freddywang.pwk.logic.model.Password
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.security.AccessController.getContext

class EditActivity : AppCompatActivity() {
    private val topAppBar: MaterialToolbar by lazy { findViewById<MaterialToolbar>(R.id.topAppBar) }
    private val focusTextInputLayout: TextInputLayout by lazy { findViewById(R.id.focus) }
    private val textFieldDes: TextInputEditText by lazy { findViewById<TextInputEditText>(R.id.textField_des) }
    private val textFieldAccount: TextInputEditText by lazy { findViewById<TextInputEditText>(R.id.textField_account) }
    private val textFieldPassword: TextInputEditText by lazy { findViewById<TextInputEditText>(R.id.textField_password) }
    private var way: Int = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        way = intent.getIntExtra("way", 1)
        var id: Long = 0
        if (way == 2) {
            val bundle = intent.extras
            if (bundle != null) {
                id = bundle["id"] as Long
                textFieldDes.text = SpannableStringBuilder(bundle["des"].toString())
                textFieldAccount.text = SpannableStringBuilder(bundle["account"].toString())
                textFieldPassword.text = SpannableStringBuilder(bundle["password"].toString())
            }
        }
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
                        val p = Password(
                            textFieldDes.text.toString(),
                            textFieldAccount.text.toString(),
                            textFieldPassword.text.toString()
                        )
                        p.id = id
                        viewModel.updatePw(p)
                        Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show()
                        finish()
                    } else if (way == 1) {
                        var result = viewModel.addPw(
                            Password(
                                textFieldDes.editableText.toString(),
                                textFieldAccount.editableText.toString(),
                                textFieldPassword.editableText.toString()
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
}