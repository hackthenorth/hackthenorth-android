package com.hackthenorth.android.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Prize {

    private static final String TAG = "Prize";

    public String id;
    public String company;
    public String contact;
    public String description;
    public String name;

    // A list of prizes, i.e. [$10k, a chromebook, free tour of the Facebook SF office]
    public ArrayList<String> prize;

    // This method takes a JSON string and returns an ArrayList of Prize from
    // the JSON data.
    public static ArrayList<Prize> loadPrizesFromJSON(String json) {

        // Deserialize using GSON.
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, Prize>>(){}.getType();
        HashMap<String, Prize> prizesMap = gson.fromJson(json, type);

        // Build an ArrayList of schedule items from the hashmap.
        ArrayList<Prize> prizes = new ArrayList<Prize>(prizesMap.size());
        for (Map.Entry<String, Prize> entry : prizesMap.entrySet()) {
            Prize prize = entry.getValue();
            prize.id = entry.getKey();
            prizes.add(prize);
        }

        Collections.sort(prizes, new Comparator<Prize>() {
            @Override
            public int compare(Prize lhs, Prize rhs) {
                // Put things without a name at the end of the list.
                if (lhs.name == null) {
                    return rhs.name == null ? 0 : 1;
                }
                return lhs.name.compareTo(rhs.name);
            }
        });

        return prizes;
    }
}
