package com.freddywang.pwk.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.freddywang.pwk.R
import com.freddywang.pwk.logic.jsonToList
import com.freddywang.pwk.logic.listToJson
import com.freddywang.pwk.logic.model.Password
import com.freddywang.pwk.ui.edit.EditActivity
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class MainActivity : AppCompatActivity() {
    private val recyclerView: RecyclerView by lazy { findViewById(R.id.recyclerView) }
    private val topAppBar: MaterialToolbar by lazy { findViewById(R.id.topAppBar) }
    private val requestCodeInput = 1
    private val requestCodeOutput = 2
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
        /*recyclerView.setHasFixedSize(true)
        list = viewModel.outPutAllPassword()
        adapter = DataListAdapter(this, list, viewModel)
        recyclerView.adapter = adapter*/
        updateList(viewModel.outPutAllPassword())
        /* viewModel.loadAllPassword().observe(
             this
         ) { updateList(it) }*/
        // 设置工具栏菜单项颜色
        setupToolbarMenuColors()
        
        topAppBar.setNavigationOnClickListener {
            val intent = Intent(this, EditActivity::class.java)
            intent.putExtra("way", 1)
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
        updateList(viewModel.outPutAllPassword())
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
                val list: List<Password> = jsonToList(text)
                viewModel.cleanTable()
                for (i in list.indices) {
                    try {
                        viewModel.addPw(list[i])
                    } catch (e: SQLiteConstraintException) {
                        e.printStackTrace()
                        Toast.makeText(this, "导入失败", Toast.LENGTH_SHORT).show()
                        break
                    }
                }
                Toast.makeText(this, "导入完成", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == requestCodeOutput) {
            if (resultCode == RESULT_OK && data != null) {
                val uri = data.data
                val outputStream = contentResolver.openOutputStream(uri!!)
                val writer = BufferedWriter(OutputStreamWriter(outputStream))
                val list: ArrayList<Password>? = viewModel.outPutAllPassword()
                val text: String? = list?.let { listToJson(it) }
                writer.write(text)
                writer.close()
                Toast.makeText(this, "导出完成", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateList(list: ArrayList<Password>?) {
        //TODO:p90,adapter.notifyDataSetChanged()
//        adapter.notifyDataSetChanged()
        adapter = DataListAdapter(this, list, viewModel)
        recyclerView.adapter = adapter
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

    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            updateList(viewModel.outPutAllPassword())
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