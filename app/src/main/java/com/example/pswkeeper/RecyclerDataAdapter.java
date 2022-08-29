package com.example.pswkeeper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class RecyclerDataAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private List<PasswordInfo> passwordInfoList;
    private final PasswordDBHelper mHelper;

    public RecyclerDataAdapter(List<PasswordInfo> passwordInfoList,
                               Context context, PasswordDBHelper helper) {
        this.passwordInfoList = passwordInfoList;
        this.mContext = context;
        mHelper = helper;
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.data_item, parent, false);
        return new DataViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DataViewHolder dataViewHolder = (DataViewHolder) holder;
        final int position1 = holder.getAdapterPosition();
        dataViewHolder.aboutTv.setText(passwordInfoList.get(position1).about);
        dataViewHolder.accountTv.setText(passwordInfoList.get(position1).account);
        dataViewHolder.passwordTv.setText(passwordInfoList.get(position1).password);
        dataViewHolder.ll_item.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("删除");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mHelper.closeLink();
                        mHelper.openWriteLink(); // 打开数据库帮助器的写连接
                        mHelper.delete("rowid=" + passwordInfoList.get(position1).rowid);// 删除记录
                        mHelper.closeLink(); // 关闭数据库连接
                        mHelper.openReadLink(); // 打开数据库帮助器的读连接
                        notifyItemRemoved(position1);
                        passwordInfoList.remove(position1);
                        notifyItemRangeChanged(position1, getItemCount());
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            }
        });
        dataViewHolder.ll_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO:进入编辑页面
                Intent intent = new Intent(mContext, EditActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("about", passwordInfoList.get(position1).about);
                bundle.putString("account", passwordInfoList.get(position1).account);
                bundle.putString("password", passwordInfoList.get(position1).password);
                bundle.putInt("xuhao", passwordInfoList.get(position1).xuhao);
                bundle.putLong("rowid", passwordInfoList.get(position1).rowid);
                intent.putExtras(bundle);
                ((SearchActivity) mContext).startActivityForResult(intent, 0);
            }
        });
    }

    @Override
    public int getItemCount() {
        return passwordInfoList.size();
    }

    static class DataViewHolder extends RecyclerView.ViewHolder {
        TextView aboutTv;
        TextView accountTv;
        TextView passwordTv;
        LinearLayout ll_item;

        public DataViewHolder(@NonNull View itemView) {
            super(itemView);
            this.ll_item = itemView.findViewById(R.id.ll_item);
            this.aboutTv = itemView.findViewById(R.id.tv_about);
            this.accountTv = itemView.findViewById(R.id.tv_account);
            this.passwordTv = itemView.findViewById(R.id.tv_password);
        }
    }
}
