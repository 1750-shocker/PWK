package com.example.pwkeeper.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "PasswordInfo")
public class PasswordInfo {

    @DatabaseField(generatedId = true)
    public int xuHao;

    @DatabaseField(columnName = "des")
    public String des;

    @DatabaseField(columnName = "account")
    public String account;

    @DatabaseField(columnName = "password")
    public String password;

    public PasswordInfo() {}

    public PasswordInfo(int xuHao, String des, String account, String password) {
        this.xuHao = xuHao;
        this.des = des;
        this.account = account;
        this.password = password;
    }

    public int getXuHao() {
        return xuHao;
    }

    public void setXuHao(int xuHao) {
        this.xuHao = xuHao;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "PasswordInfo{" +
                "xuHao=" + xuHao +
                ", des='" + des + '\'' +
                ", account='" + account + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
