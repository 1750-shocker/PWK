package com.example.pwkeeper.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pwkeeper.MainActivity;
import com.example.pwkeeper.R;
import com.example.pwkeeper.adapter.RecyclerDataAdapter;
import com.example.pwkeeper.bean.PasswordInfo;
import com.example.pwkeeper.util.MyDBHelper;
import com.example.pwkeeper.util.ViewUtil;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "ccc";
    private EditText et_search;
    private RecyclerView recyclerView;
    private List<PasswordInfo> passwordInfoList = new ArrayList<>();
    private boolean whetherSearching = false;
    private RecyclerDataAdapter recyclerDataAdapter;
    private Dao<PasswordInfo, Integer> pwInfoDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        MyDBHelper myDBHelper = MyDBHelper.getInstance(this);
        pwInfoDao = myDBHelper.getPwInfoDao();
        initView();
        initSearchBar();
    }

    private void initView() {
        et_search = findViewById(R.id.et_search);
        recyclerView = findViewById(R.id.rv_data);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        try {
            passwordInfoList = pwInfoDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        recyclerDataAdapter = new RecyclerDataAdapter(this, passwordInfoList, pwInfoDao);
        recyclerView.setAdapter(recyclerDataAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initSearchBar() {
        et_search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                ViewUtil.hideOneInputMethod(this, et_search);
                String s = et_search.getText().toString();//检索关键词并改变 RecyclerView mHelper.query("about LIKE " + "'%" + s + "%'");
                QueryBuilder<PasswordInfo, Integer> queryBuilder = pwInfoDao.queryBuilder();
                try {
                    queryBuilder.where().like("des", "%" + s + "%");
                    passwordInfoList = queryBuilder.query();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                recyclerDataAdapter.notifyDataSetChanged();
                recyclerDataAdapter = new RecyclerDataAdapter(this, passwordInfoList, pwInfoDao);
                recyclerView.setAdapter(recyclerDataAdapter);
                whetherSearching = true;
                return true;
            }
            return false;
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    void refreshList() {
        try {
            passwordInfoList = pwInfoDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        recyclerDataAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshList();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void onBackPressed() {
        if (whetherSearching) {
            refreshList();
            et_search.setText("");
            whetherSearching = false;
        } else {
            finish();
        }
    }
}