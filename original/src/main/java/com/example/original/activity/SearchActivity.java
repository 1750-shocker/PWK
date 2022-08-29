package com.example.original.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import com.example.original.R;
import com.example.original.adapter.RecyclerDataAdapter;
import com.example.original.bean.PasswordInfo;
import com.example.original.database.MyDBHelper;
import com.example.original.database.PasswordDBHelper;
import com.example.original.util.ViewUtil;

import java.sql.SQLException;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private PasswordDBHelper mHelper;
    private EditText et_search;
    private RecyclerView recyclerView;
    private List<PasswordInfo> passwordInfoList;
    private RecyclerView.Adapter recyclerDataAdapter;
    private boolean whetherSearching = false;
    public static final String TABLE_NAME = "password_info";
    private MyDBHelper mHelper1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initView();
        initSearchBar();
    }

    private void initView() {
        recyclerView = findViewById(R.id.rv_data);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
    }

    private void initSearchBar() {
        et_search = findViewById(R.id.et_search);
        et_search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                ViewUtil.hideOneInputMethod(this, et_search);
                //TODO:检索关键词并改变 RecyclerView
                String s = et_search.getText().toString();
//                mHelper.closeLink();
//                mHelper.openWriteLink();
//                List<PasswordInfo> list = mHelper.query("about LIKE " + "'%" + s + "%'");
                List<PasswordInfo> list = null;
                try {
                    list = mHelper1.getPwInfoDao().queryBuilder().where().like("about", "%" + s + "%").query();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
//                mHelper.closeLink();
//                mHelper.openReadLink();
                initRecyclerView(list);
                whetherSearching = true;
                return true;
            }
            return false;
        });
    }

//    protected void onStart() {
//        super.onStart();
//        mHelper = PasswordDBHelper.getInstance(this, 1);
//        mHelper.closeLink();
//        mHelper.openReadLink();
//        refreshAll();
//    }
//
//    protected void onStop() {
//        super.onStop();
//        mHelper.closeLink();
//    }


    @Override
    protected void onStart() {
        super.onStart();
        mHelper1 = MyDBHelper.getInstance(this);
        refreshAll();//尝试取消该语句，测试修改返回该活动时是否已更新？
    }

    public void refreshAll() {
//        if (mHelper == null) {
//            Toast.makeText(this, "数据库连接为空", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        passwordInfoList = mHelper.query("1=1");
        try {
            passwordInfoList = mHelper1.getPwInfoDao().queryForAll();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        initRecyclerView(passwordInfoList);
    }

    public void initRecyclerView(List<PasswordInfo> list) {
        recyclerDataAdapter = new RecyclerDataAdapter(list, this, mHelper);
        recyclerView.setAdapter(recyclerDataAdapter);
    }



    public void onBackPressed() {
        if (whetherSearching) {
            initRecyclerView(passwordInfoList);
            et_search.setText("");
            whetherSearching = false;
        } else {
            finish();
        }

    }


}