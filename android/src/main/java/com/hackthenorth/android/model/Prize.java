package com.hackthenorth.android.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Prize extends Model implements Comparable<Prize> {

    private static final String TAG = "Prize";

    public String company;
    public String contact;
    public String description;
    public String name;

    // A list of prizes, i.e. [$10k, a chromebook, free tour of the Facebook SF office]
    public ArrayList<String> prize;

    @Override
    public int compareTo(Prize another) {
        // Put things without a name at the end of the list.
        if (name == null) {
            return another.name == null ? 0 : 1;
        }
        return name.compareTo(another.name);
    }
}
