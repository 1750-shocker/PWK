package com.example.pwkeeper.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.pwkeeper.R;
import com.example.pwkeeper.bean.PasswordInfo;
import com.example.pwkeeper.util.MyDBHelper;
import com.example.pwkeeper.util.ViewUtil;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

public class EditActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView et_about;
    private TextView et_account;
    private TextView et_password;
    private int xuHao;
    private Button btn_saveChange;
    private Toolbar tb_head;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        initView();
    }

    private void initView() {
        tb_head = findViewById(R.id.tb_head);
        et_about = findViewById(R.id.et_about);
        et_account = findViewById(R.id.et_account);
        et_password = findViewById(R.id.et_password);
        setSupportActionBar(tb_head);
        Bundle bundle = getIntent().getExtras();
        xuHao = bundle.getInt("xuHao");
        et_about.setText(bundle.get("des").toString());
        et_account.setText(bundle.get("account").toString());
        et_password.setText(bundle.get("password").toString());
        btn_saveChange = findViewById(R.id.btn_saveChange);
        btn_saveChange.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btn_saveChange) {
            ViewUtil.hideOneInputMethod(this, et_about);
            String des = et_about.getText().toString();
            String account = et_account.getText().toString();
            String password = et_password.getText().toString();
            PasswordInfo passwordInfo = new PasswordInfo();
            passwordInfo.des = des;
            passwordInfo.account = account;
            passwordInfo.password = password;
            passwordInfo.xuHao = xuHao;
            MyDBHelper myDBHelper = MyDBHelper.getInstance(this);
            Dao<PasswordInfo, Integer> pwInfoDao = myDBHelper.getPwInfoDao();
            try {
                pwInfoDao.update(passwordInfo);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(this, SearchActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show();
            startActivity(intent);
        }
    }
}