package com.hackthenorth.android.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;

public class Mentor extends Model {
    private static final String TAG = "Mentor";

    public String id;

    // Fields
    public String name;
    public String image;
    public String organization;
    public ArrayList<ArrayList<String>> availability;
    public ArrayList<String> skills;

    public Mentor() {
    }

    // This method takes a JSON string and returns an ArrayList of Mentor from
    // the JSON data.
    public static ArrayList<Mentor> loadMentorArrayFromJSON(String json) {

        // Deserialize using GSON.
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, Mentor>>(){}.getType();
        HashMap<String, Mentor> mentorMap = gson.fromJson(json, type);

        // Build an ArrayList of mentors from the hashmap.
        ArrayList<Mentor> mentors = new ArrayList<Mentor>(mentorMap.size());
        for (Map.Entry<String, Mentor> entry : mentorMap.entrySet()) {
            Mentor mentor = entry.getValue();
            mentor.id = entry.getKey();
            mentors.add(mentor);
        }

        // Sort the list
        Collections.sort(mentors, new Comparator<Mentor>() {
            @Override
            public int compare(Mentor lhs, Mentor rhs) {
                // Sort them in descending order---that's why rhs is on the lhs
                // in this expression.
                return rhs.id.compareTo(lhs.id);
            }
        });

        return mentors;
    }
}
