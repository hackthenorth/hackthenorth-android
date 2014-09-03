package com.hackthenorth.android.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hackthenorth.android.framework.FuzzySearchIndexer;

import java.lang.reflect.Type;
import java.util.*;

public class Mentor extends Model implements FuzzySearchIndexer.Tokened, Comparable<Mentor> {
    private static final String TAG = "Mentor";

    // Fields
    public String name;
    public String image;
    public String organization;
    public ArrayList<ArrayList<String>> availability;
    public ArrayList<String> skills;
    public String email;
    public String twitter;
    public String github;
    public String phone;

    @Override
    public ArrayList<String> getTokens() {
        ArrayList<String> result = new ArrayList<String>(skills);
        result.add(organization);
        result.addAll(Arrays.asList(name.split(" ")));
        return result;
    }

    @Override
    public int compareTo(Mentor another) {
        if (name == null) {
            return 1;
        } else if (another == null) {
            return -1;
        } else {
            return name.compareTo(another.name);
        }
    }
}
