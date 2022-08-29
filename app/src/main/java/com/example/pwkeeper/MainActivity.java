package com.example.pwkeeper;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.pwkeeper.activity.SearchActivity;
import com.example.pwkeeper.bean.PasswordInfo;
import com.example.pwkeeper.util.GsonUtil;
import com.example.pwkeeper.util.MyDBHelper;
import com.example.pwkeeper.util.PermissionUtil;
import com.example.pwkeeper.util.ViewUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.dao.Dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "aaa";
    private Button btn_check;
    private Button btn_save;
    private Button btnRead;
    private Button btnWrite;
    private Button btnPermission;

    private EditText et_password;
    private EditText et_about;
    private EditText et_account;
    private TextView tv_dbpath;
    private Toolbar tb_head;
    private Dao<PasswordInfo, Integer> pwInfoDao;

    private String mPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyDBHelper myDBHelper = MyDBHelper.getInstance(this);
        pwInfoDao = myDBHelper.getPwInfoDao();
        initView();
    }

    private void initView() {
        tb_head = findViewById(R.id.tb_head);
        btn_check = findViewById(R.id.btn_check);
        btn_save = findViewById(R.id.btn_save);
        et_password = findViewById(R.id.et_password);
        et_about = findViewById(R.id.et_about);
        et_account = findViewById(R.id.et_account);
        tv_dbpath = findViewById(R.id.tv_dbpath);
        btnRead = (Button) findViewById(R.id.btn_read);
        btnWrite = (Button) findViewById(R.id.btn_write);
        setSupportActionBar(tb_head);
        btn_check.setOnClickListener(this);
        btn_save.setOnClickListener(this);
        btnRead.setOnClickListener(this);
        btnWrite.setOnClickListener(this);
        btnPermission = (Button) findViewById(R.id.btn_permission);
        btnPermission.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK && data != null) {
                Uri uri = data.getData();
                Log.d(TAG, "onActivityResult: 拿到uri了");
                String text = "";
                try {
                    Log.d(TAG, "onActivityResult: 读取到1：" + text);
                    String path = "/storage/emulated/0/Download/data(1).txt";
                    text = openText(uri);
//                    text = load();
                    Log.d(TAG, "onActivityResult: 读取到2：" + text);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                List<PasswordInfo> list = GsonUtil.jsonToList(text);
                PasswordInfo r = new PasswordInfo();
                if (list != null) {
                    for (int i = 0; i < list.size(); i++) {
                        r = list.get(i);
                        try {
                            pwInfoDao.create(r);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else if (requestCode == 2) {
            if (resultCode == RESULT_OK && data != null) {
                Uri uri = data.getData();
                try {
                    writeText(uri);
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String openText(Uri uri) throws IOException {
        StringBuilder builder = new StringBuilder();
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line = null;
        while (((line = reader.readLine()) != null)) {
            builder.append(line);
        }
        inputStream.close();
//        int available = inputStream.available();
//        byte[] b = new byte[available];
//        inputStream.read(b);
//        readStr = new String(b);
//        inputStream.close();
        return builder.toString();
    }

    private String load() throws IOException {
        StringBuilder content = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(openFileInput("data")));
        String line = null;
        while (((line = reader.readLine()) != null)) {
            content.append(line);
        }
        return content.toString();
    }

    private void writeText(Uri uri) throws IOException, SQLException {
        OutputStream outputStream = getContentResolver().openOutputStream(uri);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        List<PasswordInfo> list = pwInfoDao.queryForAll();
        Log.d(TAG, "writeText: " + list);
        String text = GsonUtil.listToJson(list);
        Log.d(TAG, "writeText: 正在写入" + text);
//        String filePath = mPath + "password.txt";
//        Log.d(TAG, "writeText: " + filePath);
//        saveText(filePath, text);
        writer.write(text);
        writer.close();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_check) {
            ViewUtil.hideOneInputMethod(this, et_password);
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.btn_save) {
            ViewUtil.hideOneInputMethod(this, et_password);
            String des = et_about.getText().toString();
            String account = et_account.getText().toString();
            String password = et_password.getText().toString();
            if (TextUtils.isEmpty(des)) {
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
            passwordInfo.des = des;
            passwordInfo.account = account;
            passwordInfo.password = password;
            passwordInfo.xuHao = 0;
            try {
                pwInfoDao.create(passwordInfo);
            } catch (SQLException e) {
                e.printStackTrace();
                Log.i(TAG, "onClick: 添加出错");
            }
            Toast.makeText(this, "数据已写入", Toast.LENGTH_SHORT).show();
            et_about.setText("");
            et_account.setText("");
            et_password.setText("");
        } else if (v.getId() == R.id.btn_read) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, 1);
        } else if (v.getId() == R.id.btn_write) {
            List<PasswordInfo> list = null;
            try {
                list = pwInfoDao.queryForAll();
                Log.d(TAG, "data: " + list);
                String text = GsonUtil.listToJson(list);
                Log.d(TAG, "dataText:" + text);
                FileOutputStream outputStream = openFileOutput("data", Context.MODE_PRIVATE);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                writer.write(text);
                writer.close();
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_TITLE, "password.txt");
            startActivityForResult(intent, 2);
        } else if (v.getId() == R.id.btn_permission) {
            PermissionUtil.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, R.id.btn_permission % 65535);
        }
    }

    public void saveText(String path, String txt) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(path);
        fileOutputStream.write(txt.getBytes());
        fileOutputStream.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // requestCode不能为负数，也不能大于2的16次方即65536
        if (requestCode == R.id.btn_permission % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                Toast.makeText(this, "ok", Toast.LENGTH_SHORT).show();
            } else {
                //ToastUtil.show(this, "需要允许存储卡权限才能写入公共空间噢");
                Toast.makeText(this, "需要允许存储卡权限才能写入公共空间噢", Toast.LENGTH_SHORT).show();
            }
        }
    }
}