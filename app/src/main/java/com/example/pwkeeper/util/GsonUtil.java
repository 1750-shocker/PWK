package com.example.pwkeeper.util;

import com.example.pwkeeper.bean.PasswordInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GsonUtil {
    public static final Gson GSON = new Gson();

    public static List<PasswordInfo> jsonToList(String json) {
        if (Objects.isNull(json)) return null;
        try {
            // 需要注意这里的type
            Type type = new TypeToken<List<PasswordInfo>>(){}.getType();
            return GSON.fromJson(json, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String listToJson(List list) {
        if (Objects.isNull(list)) return "";
        try {
            Gson gson = new Gson();
            return gson.toJson(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
