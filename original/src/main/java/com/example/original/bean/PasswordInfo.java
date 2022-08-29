package com.example.original.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "passwordInfo")
public class PasswordInfo {
    public long rowid;
    @DatabaseField(generatedId = true)
    public int xuhao;
    @DatabaseField
    public String about;
    @DatabaseField
    public String account;
    @DatabaseField
    public String password;

    public PasswordInfo() {
        rowid = 0L;
        xuhao = 0;
        about = "";
        account = "";
        password = "";
    }

    public PasswordInfo(long rowid, int xuhao, String about, String account, String password) {
        this.rowid = rowid;
        this.xuhao = xuhao;
        this.about = about;
        this.account = account;
        this.password = password;
    }

    @Override
    public String toString() {
        return "PasswordInfo{" +
                "rowid=" + rowid +
                ", xuhao=" + xuhao +
                ", about='" + about + '\'' +
                ", account='" + account + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public long getRowid() {
        return rowid;
    }

    public void setRowid(long rowid) {
        this.rowid = rowid;
    }

    public int getXuhao() {
        return xuhao;
    }

    public void setXuhao(int xuhao) {
        this.xuhao = xuhao;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
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
}
