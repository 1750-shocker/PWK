package com.freddywang.pwk.ui.home

import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.freddywang.pwk.R
import com.freddywang.pwk.logic.model.Password
import com.freddywang.pwk.ui.edit.EditActivity
import java.sql.SQLException

class DataListAdapter(
    private val context: Context,
    private val passwordList: ArrayList<Password>?,
    private val viewModel: MainViewModel
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val passwordVisibility = mutableMapOf<Int, Boolean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_pwlist, parent, false)
        return DataViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: List<Any?>
    ) {
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val dataViewHolder = holder as DataViewHolder
        dataViewHolder.aboutTv.text = passwordList!![position].des
        dataViewHolder.accountTv.text = passwordList[position].account
        
        // 设置密码显示状态
        val isPasswordVisible = passwordVisibility[position] ?: false
        dataViewHolder.passwordTv.text = if (isPasswordVisible) {
            passwordList[position].password
        } else {
            "•".repeat(8) // 显示8个黑点
        }
        
        // 设置开关状态
        dataViewHolder.visibilitySwitch.isChecked = isPasswordVisible
        dataViewHolder.visibilitySwitch.setOnCheckedChangeListener { _, isChecked ->
            passwordVisibility[position] = isChecked
            dataViewHolder.passwordTv.text = if (isChecked) {
                passwordList[position].password
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
                    viewModel.deletePw(passwordList[position])
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
                notifyItemRemoved(position)//这里应该由liveData观察回调触发
                passwordList.removeAt(position)
                notifyItemRangeChanged(position, itemCount)

            }
            builder.setNegativeButton(
                "编辑"
            ) { _: DialogInterface?, _: Int ->
                val intent = Intent(context, EditActivity::class.java)
                val bundle = Bundle().apply {
                    putLong("id", passwordList[position].id)
                    putString("des", passwordList[position].des)
                    putString("account", passwordList[position].account)
                    putString("password", passwordList[position].password)
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
            val clip: ClipData =
                ClipData.newPlainText("password text", passwordList[position].password)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "密码已复制", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return passwordList?.size ?: 0
    }

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var aboutTv: TextView
        var accountTv: TextView
        var passwordTv: TextView
        var ll: LinearLayout
        var visibilitySwitch: SwitchCompat
        
        init {
            ll = itemView.findViewById(R.id.ll_item)
            aboutTv = itemView.findViewById(R.id.tv_about)
            accountTv = itemView.findViewById(R.id.tv_account)
            passwordTv = itemView.findViewById(R.id.tv_password)
            visibilitySwitch = itemView.findViewById(R.id.switch_visibility)
        }
    }

}