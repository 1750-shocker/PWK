package com.example.original;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.original.activity.SearchActivity;
import com.example.original.bean.PasswordInfo;
import com.example.original.database.MyDBHelper;
import com.example.original.database.PasswordDBHelper;
import com.example.original.util.ViewUtil;

import java.sql.SQLException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private PasswordDBHelper mHelper;
    private Button btn_check;
    private Button btn_save;
    private EditText et_password;
    private EditText et_about;
    private EditText et_account;
    private TextView tv_dbpath;
    private MyDBHelper mHelper1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }
    private void initView() {
        Toolbar tb_head = findViewById(R.id.tb_head);
        tb_head.setTitle(R.string.password_keeper);
        setSupportActionBar(tb_head);
        btn_check = findViewById(R.id.btn_check);
        btn_check.setOnClickListener(this);
        btn_save = findViewById(R.id.btn_save);
        btn_save.setOnClickListener(this);
        et_password = findViewById(R.id.et_password);
        et_about = findViewById(R.id.et_about);
        et_account = findViewById(R.id.et_account);
        tv_dbpath = findViewById(R.id.tv_dbpath);
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        // 获得数据库帮助器的实例
//        mHelper = PasswordDBHelper.getInstance(this, 1);
//        SQLiteDatabase db = mHelper.openWriteLink();// 打开数据库帮助器的写连接
//        String dbPath = String.format("数据库路径：%s", db.getPath());
//        tv_dbpath.setText(dbPath);
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        mHelper.closeLink(); // 关闭数据库连接
//    }


    @Override
    protected void onStart() {
        super.onStart();
        mHelper1 = MyDBHelper.getInstance(this);
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        mHelper1.close();
//    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_check) {
            ViewUtil.hideOneInputMethod(this, et_password);
            Intent intent = new Intent(this, SearchActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } else {
            ViewUtil.hideOneInputMethod(this, et_password);
            String about = et_about.getText().toString();
            String account = et_account.getText().toString();
            String password = et_password.getText().toString();
            if (TextUtils.isEmpty(about)) {
                Toast.makeText(this, "未填写描述", Toast.LENGTH_SHORT).show();
                return;
            } else if (TextUtils.isEmpty(account)) {
                Toast.makeText(this, "未填写账号", Toast.LENGTH_SHORT).show();
                return;
            } else if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "未填写密码", Toast.LENGTH_SHORT).show();
                return;
            }
            PasswordInfo passwordInfo = new PasswordInfo();
            passwordInfo.about = about;
            passwordInfo.account = account;
            passwordInfo.password = password;
//            mHelper.openWriteLink();
//            mHelper.insert(passwordInfo);
            try {
                int i = mHelper1.getPwInfoDao().create(passwordInfo);
                Toast.makeText(this, "数据已写入"+i, Toast.LENGTH_SHORT).show();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            et_about.setText("");
            et_account.setText("");
            et_password.setText("");
        }
    }
}