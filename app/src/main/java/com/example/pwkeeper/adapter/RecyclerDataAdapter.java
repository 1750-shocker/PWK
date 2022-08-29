package com.example.pwkeeper.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pwkeeper.R;
import com.example.pwkeeper.activity.EditActivity;
import com.example.pwkeeper.activity.SearchActivity;
import com.example.pwkeeper.bean.PasswordInfo;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

public class RecyclerDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context mContext;
    private final List<PasswordInfo> passwordInfoList;
    private final Dao<PasswordInfo, Integer> pwInfoDao;

    public RecyclerDataAdapter(Context context, List<PasswordInfo> passwordInfoList,
                               Dao<PasswordInfo, Integer> dao) {
        this.passwordInfoList = passwordInfoList;
        this.mContext = context;
        this.pwInfoDao = dao;
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.data_item, parent, false);
        return new DataViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DataViewHolder dataViewHolder = (DataViewHolder) holder;
        dataViewHolder.aboutTv.setText(passwordInfoList.get(position).des);
        dataViewHolder.accountTv.setText(passwordInfoList.get(position).account);
        dataViewHolder.passwordTv.setText(passwordInfoList.get(position).password);
        dataViewHolder.ll_item.setOnLongClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("删除");
            builder.setPositiveButton("确定", (dialog, which) -> {
                try {
                    pwInfoDao.deleteById(passwordInfoList.get(position).xuHao);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                notifyItemRemoved(position);
                passwordInfoList.remove(position);
                notifyItemRangeChanged(position, getItemCount());
            });
            builder.setNegativeButton("取消", (dialog, which) -> {
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return true;
        });
        dataViewHolder.ll_item.setOnClickListener(v -> {
            //TODO:进入编辑页面
            Intent intent = new Intent(mContext, EditActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("des", passwordInfoList.get(position).des);
            bundle.putString("account", passwordInfoList.get(position).account);
            bundle.putString("password", passwordInfoList.get(position).password);
            bundle.putInt("xuHao", passwordInfoList.get(position).xuHao);
            intent.putExtras(bundle);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            ((SearchActivity) mContext).startActivity(intent);
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
