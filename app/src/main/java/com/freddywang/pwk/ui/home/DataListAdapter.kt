package com.freddywang.pwk.ui.home

import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.card.MaterialCardView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freddywang.pwk.R
import com.freddywang.pwk.logic.model.Password
import com.freddywang.pwk.ui.edit.EditActivity
import com.freddywang.pwk.util.CryptoUtil
import java.sql.SQLException

class PasswordDiffCallback : DiffUtil.ItemCallback<Password>() {
    override fun areItemsTheSame(oldItem: Password, newItem: Password): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Password, newItem: Password): Boolean {
        return oldItem == newItem
    }
}

class DataListAdapter(
    private val context: Context,
    private val viewModel: MainViewModel
) : ListAdapter<Password, DataListAdapter.DataViewHolder>(PasswordDiffCallback()) {

    private val passwordVisibility = mutableMapOf<Long, Boolean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_pwlist, parent, false)
        return DataViewHolder(view)
    }



    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        val password = getItem(position)
        val dataViewHolder = holder
        dataViewHolder.aboutTv.text = password.des
        dataViewHolder.accountTv.text = password.account
        
        // 设置密码显示状态
        val isPasswordVisible = passwordVisibility[password.id] ?: false
        dataViewHolder.passwordTv.text = if (isPasswordVisible) {
            // 解密密码用于显示
            if (password.isEncrypted) {
                try {
                    CryptoUtil.decrypt(context, password.password)
                } catch (e: Exception) {
                    "解密失败"
                }
            } else {
                password.password
            }
        } else {
            "•".repeat(8) // 显示8个黑点
        }
        
        // 设置开关状态
        dataViewHolder.visibilitySwitch.isChecked = isPasswordVisible
        dataViewHolder.visibilitySwitch.setOnCheckedChangeListener { _, isChecked ->
            passwordVisibility[password.id] = isChecked
            dataViewHolder.passwordTv.text = if (isChecked) {
                password.password
            } else {
                "•".repeat(8)
            }
        }

        dataViewHolder.ll.setOnLongClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("删除/编辑")
            builder.setPositiveButton(
                "删除"
            ) { _: DialogInterface?, _: Int ->
                try {
                    viewModel.deletePw(password)
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
                // 这里应该由liveData观察回调触发更新
                // 手动移除数据项会导致与LiveData数据不同步

            }
            builder.setNegativeButton(
                "编辑"
            ) { _: DialogInterface?, _: Int ->
                val intent = Intent(context, EditActivity::class.java)
                val bundle = Bundle().apply {
                    putLong("id", password.id)
                    putString("des", password.des)
                    putString("account", password.account)
                    putString("password", password.password)
                }
                intent.putExtra("way", 2)
                intent.putExtras(bundle)
                context.startActivity(intent)
            }
            val alertDialog = builder.create()
            alertDialog.show()
            true
        }
        dataViewHolder.ll.setOnClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val passwordToCopy = if (password.isEncrypted) {
                try {
                    CryptoUtil.decrypt(context, password.password)
                } catch (e: Exception) {
                    password.password
                }
            } else {
                password.password
            }
            val clip: ClipData =
                ClipData.newPlainText("password text", passwordToCopy)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "密码已复制", Toast.LENGTH_SHORT).show()
        }
    }

    fun submitPasswordList(passwords: List<Password>?) {
        submitList(passwords)
    }

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var aboutTv: TextView = itemView.findViewById(R.id.tv_about)
        var accountTv: TextView = itemView.findViewById(R.id.tv_account)
        var passwordTv: TextView = itemView.findViewById(R.id.tv_password)
        var ll: MaterialCardView = itemView.findViewById(R.id.ll_item)
        var visibilitySwitch: SwitchCompat = itemView.findViewById(R.id.switch_visibility)
    }

}