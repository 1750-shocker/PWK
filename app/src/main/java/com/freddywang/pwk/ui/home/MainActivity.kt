package com.freddywang.pwk.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.freddywang.pwk.R
import com.freddywang.pwk.logic.jsonToList
import com.freddywang.pwk.logic.listToJson
import com.freddywang.pwk.logic.model.Password
import com.freddywang.pwk.ui.edit.EditActivity
import com.freddywang.pwk.util.CryptoUtil
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class MainActivity : AppCompatActivity() {
    companion object{
        const val TAG = "wzhhh"
    }
    private val recyclerView: RecyclerView by lazy { findViewById(R.id.recyclerView) }
    private val topAppBar: MaterialToolbar by lazy { findViewById(R.id.topAppBar) }
    private val requestCodeInput = 1
    private val requestCodeOutput = 2
    private val requestCodeLegacyInput = 3
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: DataListAdapter
    private var searchJob: Job? = null
    private val searchScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = manager
        // 启用视图回收池，提高滚动性能
        recyclerView.setItemViewCacheSize(20)
        recyclerView.setHasFixedSize(false) // 改为false，允许动态高度
        recyclerView.isNestedScrollingEnabled = true
        
        // 添加item间距装饰器
        val itemDecoration = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        ContextCompat.getDrawable(this, R.drawable.divider)?.let {
            itemDecoration.setDrawable(it)
        }
//        recyclerView.addItemDecoration(itemDecoration)
        
        /*recyclerView.setHasFixedSize(true)
        list = viewModel.outPutAllPassword()
        adapter = DataListAdapter(this, list, viewModel)
        recyclerView.adapter = adapter*/
        // 测试JSON导入导出完整性
//        val jsonIntegrityTest = viewModel.testJsonIntegrity()
//        Log.i(TAG, "JSON完整性测试: ${if (jsonIntegrityTest) "通过" else "失败"}")
        
        updateList(viewModel.outPutAllUIPassword())
        /* viewModel.loadAllPassword().observe(
             this
         ) { updateList(it) }*/
        // 设置工具栏菜单项颜色
        setupToolbarMenuColors()
        
        topAppBar.setNavigationOnClickListener {
            val intent = Intent(this, EditActivity::class.java)
            intent.putExtra("way", EditActivity.MODE_ADD)
            startActivity(intent)
        }

        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.input -> {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "text/plain"
                    startActivityForResult(intent, requestCodeInput)
                    true
                }
                R.id.output -> {
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "text/plain"
                    intent.putExtra(Intent.EXTRA_TITLE, "password.txt")
                    startActivityForResult(intent, requestCodeOutput)
                    true
                }
                R.id.clear_all -> {
                    showClearAllConfirmDialog()
                    true
                }
                R.id.import_legacy -> {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "text/plain"
                    startActivityForResult(intent, requestCodeLegacyInput)
                    true
                }
                R.id.search -> {
                    // Handle search icon press
                    val searchView = menuItem?.actionView as SearchView
                    searchView.queryHint = "输入关键词"
                    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            query?.let { performSearch(it) }
                            return true
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            searchJob?.cancel()
                            searchJob = searchScope.launch {
                                delay(300) // 防抖延迟300ms
                                newText?.let { performSearch(it) }
                            }
                            return true
                        }

                    })
                    // Configure the search info and add any event listeners...
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateList(viewModel.outPutAllUIPassword())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestCodeInput) {
            if (resultCode == RESULT_OK && data != null) {
                val uri = data.data
                val builder = StringBuilder()
                val inputStream = contentResolver.openInputStream(uri!!)
                val reader = BufferedReader(InputStreamReader(inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    builder.append(line)
                }
                inputStream!!.close()
                val text = builder.toString()
                Log.d("wzhhh", "导入 - 读取的JSON内容: $text")
                
                // 验证应用签名
                val signatureHash = CryptoUtil.getAppSignatureHash(this)
                Log.d("wzhhh", "导入 - 当前应用签名: $signatureHash")
                
                val list: List<Password> = jsonToList(text)
                Log.d("wzhhh", "导入 - 解析后密码数量: ${list.size}")
                list.forEachIndexed { index, password ->
                    Log.d("wzhhh", "导入 - 密码$index: des=${password.des}, account=${password.account}, password=${password.password}, isEncrypted=${password.isEncrypted}")
                }
                
                viewModel.cleanTable()
                for (i in list.indices) {
                    try {
                        viewModel.addPw2(list[i])
                    } catch (e: SQLiteConstraintException) {
                        e.printStackTrace()
                        Toast.makeText(this, "导入失败", Toast.LENGTH_SHORT).show()
                        break
                    }
                }
                // 导入完成后刷新列表
                Toast.makeText(this, "导入完成", Toast.LENGTH_SHORT).show()
                updateList(viewModel.outPutAllUIPassword())
            }
        } else if (requestCode == requestCodeOutput) {
            if (resultCode == RESULT_OK && data != null) {
                val uri = data.data
                val outputStream = contentResolver.openOutputStream(uri!!)
                val writer = BufferedWriter(OutputStreamWriter(outputStream))
                val list: ArrayList<Password>? = viewModel.outPutAllDBPassword()
                Log.d("wzhhh", "导出 - 从数据库获取密码数量: ${list?.size ?: 0}")
                list?.forEachIndexed { index, password ->
                    Log.d("wzhhh", "导出 - 密码$index: des=${password.des}, account=${password.account}, password=${password.password}, isEncrypted=${password.isEncrypted}")
                }
                // 导出时创建不包含id的数据
                val exportList = list?.map { password ->
                    ExportPassword(
                        des = password.des,
                        account = password.account,
                        password = password.password, // 直接使用已加密的密码
                        isEncrypted = password.isEncrypted
                    )
                }
                val text: String? = exportList?.let { 
                    com.google.gson.Gson().toJson(it)
                }
                Log.d("wzhhh", "导出 - JSON内容: $text")
                writer.write(text)
                writer.close()
                Toast.makeText(this, "导出完成", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == requestCodeLegacyInput) {
            if (resultCode == RESULT_OK && data != null) {
                val uri = data.data
                val builder = StringBuilder()
                val inputStream = contentResolver.openInputStream(uri!!)
                val reader = BufferedReader(InputStreamReader(inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    builder.append(line)
                }
                inputStream!!.close()
                val text = builder.toString()
                Log.d("wzhhh", "导入旧版数据 - 读取的JSON内容: $text")
                
                // 解析旧版数据
                try {
                    val gson = com.google.gson.Gson()
                    val type = object : com.google.gson.reflect.TypeToken<List<LegacyPassword>>() {}.type
                    val legacyPasswords: List<LegacyPassword> = gson.fromJson(text, type)
                    
                    Log.d("wzhhh", "解析旧版数据数量: ${legacyPasswords.size}")
                    
                    // 清空当前数据
                    viewModel.cleanTable()
                    
                    // 导入旧版数据（作为明文密码重新加密）
                    var successCount = 0
                    for (legacyPassword in legacyPasswords) {
                        try {
                            // 将旧版数据作为明文密码处理，用新密钥重新加密
                            val newPassword = Password(
                                des = legacyPassword.des,
                                account = legacyPassword.account,
                                password = legacyPassword.password, // 旧版密码作为明文
                                isEncrypted = false // 标记为明文，让addPw方法重新加密
                            )
                            
                            val result = viewModel.addPw(newPassword)
                            if (result > 0) {
                                successCount++
                                Log.d(TAG, "成功导入旧版密码: ${legacyPassword.des}")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "导入旧版密码失败: ${legacyPassword.des}, 错误: ${e.message}", e)
                        }
                    }
                    
                    // 刷新列表
                    updateList(viewModel.outPutAllUIPassword())
                    
                    if (successCount > 0) {
                        Toast.makeText(this, getString(R.string.import_legacy_success), Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "旧版数据导入完成，成功导入 $successCount 条记录")
                    } else {
                        Toast.makeText(this, getString(R.string.import_legacy_failed), Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "旧版数据导入失败，没有成功导入任何记录")
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "解析旧版数据失败: ${e.message}", e)
                    Toast.makeText(this, "解析旧版数据失败，请检查文件格式", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateList(list: ArrayList<Password>?) {
        if (::adapter.isInitialized) {
            adapter.submitPasswordList(list)
            adapter.notifyDataSetChanged()
            recyclerView.requestLayout()
        } else {
            adapter = DataListAdapter(this, viewModel)
            recyclerView.adapter = adapter
            adapter.submitPasswordList(list)
            adapter.notifyDataSetChanged()
            recyclerView.requestLayout()
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
    
    private fun showClearAllConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.clear_all_confirm_title))
            .setMessage(getString(R.string.clear_all_confirm_message))
            .setPositiveButton(getString(R.string.clear_all_confirm_yes)) { _, _ ->
                clearAllData()
            }
            .setNegativeButton(getString(R.string.clear_all_confirm_no), null)
            .show()
    }
    
    private fun clearAllData() {
        try {
            Log.d(TAG, "开始清除所有数据")
            viewModel.cleanTable()
            updateList(viewModel.outPutAllUIPassword())
            Toast.makeText(this, getString(R.string.clear_all_success), Toast.LENGTH_SHORT).show()
            Log.d(TAG, "所有数据清除完成")
        } catch (e: Exception) {
            Log.e(TAG, "清除数据失败: ${e.message}", e)
            Toast.makeText(this, "清除数据失败", Toast.LENGTH_SHORT).show()
        }
    }
    
    
    /**
     * 旧版密码数据类（没有isEncrypted字段）
     */
    data class LegacyPassword(
        val account: String,
        val des: String,
        val id: Int,
        val password: String
    )
    
    /**
     * 导出密码数据类（不包含id字段）
     */
    data class ExportPassword(
        val des: String,
        val account: String,
        val password: String,
        val isEncrypted: Boolean
    )

    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            updateList(viewModel.outPutAllUIPassword())
        } else {
            val list = viewModel.queryWithKeyWord(query)
            updateList(list)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        searchScope.cancel()
    }
}