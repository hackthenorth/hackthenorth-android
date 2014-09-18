package com.hackthenorth.android.model;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;

public class Update extends Model implements Comparable<Update> {

    private static final String TAG = "Update";

    // Fields
    public String description;
    public String name;
    public String time;
    public String avatar;

    public static Update fromBundle(Bundle bundle) {
        Update update = new Update();
        update.name = bundle.getString("name", null);
        update.description = bundle.getString("description", null);
        update.time = bundle.getString("time", null);
        update.avatar = bundle.getString("avatar", null);
        return update;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Override
    public int compareTo(@NonNull Update another) {
        return another.id.compareTo(id);
    }
}
