package com.example.pswkeeper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class EditActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView et_about;
    private TextView et_account;
    private TextView et_password;
    private Long rowid;
    private int xuhao;
    private Button btn_saveChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Toolbar tb_head = findViewById(R.id.tb_head);
        tb_head.setTitle("修改");
        setSupportActionBar(tb_head);
        initView();
    }

    private void initView() {
        et_about = findViewById(R.id.et_about);
        et_account = findViewById(R.id.et_account);
        et_password = findViewById(R.id.et_password);
        Bundle bundle = getIntent().getExtras();
        rowid = bundle.getLong("rowid");
        xuhao = bundle.getInt("xuhao");
        et_about.setText(bundle.get("about").toString());
        et_account.setText(bundle.get("account").toString());
        et_password.setText(bundle.get("password").toString());
        btn_saveChange = findViewById(R.id.btn_saveChange);
        btn_saveChange.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        ViewUtil.hideOneInputMethod(this,et_about);
        String about = et_about.getText().toString();
        String account = et_account.getText().toString();
        String password = et_password.getText().toString();
        PasswordInfo passwordInfo = new PasswordInfo();
        passwordInfo.about = about;
        passwordInfo.account = account;
        passwordInfo.password = password;
        passwordInfo.xuhao = xuhao;
        passwordInfo.rowid = rowid;
        PasswordDBHelper mHelper = PasswordDBHelper.getInstance(this, 1);
        mHelper.closeLink(); // 关闭数据库连接
        mHelper.openWriteLink();
        mHelper.update(passwordInfo);
        Intent intent = new Intent(this, SearchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }
}