package com.hackthenorth.android.model;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;

public class TeamMember extends Model implements Comparable<TeamMember> {
    private static final String TAG = "TeamMember";

    // Fields
    public String name;
    public String avatar;
    public ArrayList<String> role;
    public String phone;
    public String email;
    public String twitter;

    @Override
    public int compareTo(@NonNull TeamMember another) {
        if (name == null) {
            return -1;
        } else {
            return name.compareTo(another.name);
        }
    }
}
