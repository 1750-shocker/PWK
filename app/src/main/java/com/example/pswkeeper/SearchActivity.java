package com.example.pswkeeper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private PasswordDBHelper mHelper;
    private EditText et_search;
    private RecyclerView recyclerView;
    private List<PasswordInfo> passwordInfoList;
    private RecyclerView.Adapter recyclerDataAdapter;
    private boolean whetherSearching = false;
    public static final String TABLE_NAME = "password_info";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        recyclerView = findViewById(R.id.rv_data);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        initSearchBar();

    }

    protected void onStart() {
        super.onStart();
        mHelper = PasswordDBHelper.getInstance(this, 1);
        mHelper.closeLink();
        mHelper.openReadLink();
        readSQLite();
    }

    protected void onStop() {
        super.onStop();
        mHelper.closeLink();
    }

    public void readSQLite() {
        if (mHelper == null) {
            Toast.makeText(this, "数据库连接为空", Toast.LENGTH_SHORT).show();
            return;
        }
        passwordInfoList = mHelper.query("1=1");
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

    private void initSearchBar() {
        et_search = findViewById(R.id.et_search);
        et_search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                ViewUtil.hideOneInputMethod(this, et_search);
                //TODO:检索关键词并改变 RecyclerView
                Log.d("tttt", "initSearchBar:1 ");
                String s = et_search.getText().toString();
                mHelper.closeLink();
                mHelper.openWriteLink();
                Log.d("tttt", "initSearchBar:2 ");
                List<PasswordInfo> list = mHelper.query("about LIKE " + "'%" + s + "%'");
                mHelper.closeLink();
                mHelper.openReadLink();
                Log.d("tttt", "initSearchBar:3 ");
                initRecyclerView(list);
                Log.d("tttt", "initSearchBar:4 ");
                whetherSearching = true;
                Log.d("tttt", "initSearchBar:5 ");
                return true;
            }
            return false;
        });

    }
}