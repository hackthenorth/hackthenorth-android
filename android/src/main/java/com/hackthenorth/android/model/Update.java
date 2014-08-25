package com.hackthenorth.android.model;

import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;

public class Update extends Model {

    private static final String TAG = "Update";
    public String id;

    // Fields
    public String description;
    public String name;
    public String time;
    public String avatar;

    public Update() {
    }

    public static Update fromBundle(Bundle bundle) {
        Update update = new Update();
        update.name = bundle.getString("name", null);
        update.description = bundle.getString("description", null);
        update.time = bundle.getString("time", null);
        update.avatar = bundle.getString("avatar", null);
        return update;
    }

    // This method takes a JSON string and returns an ArrayList of Update from
    // the JSON data.
    public static ArrayList<Update> loadUpdateArrayFromJSON(String json) {

        // Deserialize using GSON.
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, Update>>(){}.getType();
        HashMap<String, Update> updateMap = gson.fromJson(json, type);

        // Build an ArrayList of updates from the hashmap.
        ArrayList<Update> updates = new ArrayList<Update>(updateMap.size());
        for (Map.Entry<String, Update> entry : updateMap.entrySet()) {
            Update update = entry.getValue();
            update.id = entry.getKey();
            updates.add(update);
        }

        // Sort the list
        Collections.sort(updates, new Comparator<Update>() {
            @Override
            public int compare(Update lhs, Update rhs) {
                // Sort them in descending order---that's why rhs is on the lhs
                // in this expression.
                return rhs.id.compareTo(lhs.id);
            }
        });

        return updates;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}