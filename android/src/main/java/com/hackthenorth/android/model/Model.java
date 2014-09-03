package com.hackthenorth.android.model;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// Polymorphism yay!
public abstract class Model {

    private static final String TAG = "Model";
    public String id;

    /**
     * @param modelList The ArrayList to fill.
     * @param json The JSON to decode.
     * @param <T> The type of model to fill the array with.
     */
    public static <T extends Model & Comparable>
    void loadArrayListFromJSON(ArrayList<T> modelList, TypeToken type, String json) {

        modelList.clear();

        // Deserialize using GSON.
        Gson gson = new Gson();
        HashMap<String, T> itemMap = gson.fromJson(json, type.getType());

        // Build an ArrayList of teamMembers from the hashmap.
        if (itemMap != null) {
            for (Map.Entry<String, T> entry : itemMap.entrySet()) {
                T model = entry.getValue();
                model.id = entry.getKey();
                modelList.add(model);
            }
        }

        // Sort the list
        Collections.sort(modelList);
    }
}
