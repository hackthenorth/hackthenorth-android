package com.hackthenorth.android.model;

import android.util.Log;

import java.util.*;

import org.json.JSONException;
import org.json.JSONObject;

public class Update {

    public String id;

    // Fields
    public String description;
    public String name;
    public String datetime;
    public String avatarUrl;

    // JSON keys for fields
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String DATETIME = "time";
    public static final String AVATAR_URL = "avatar";

    public Update() {
    }

    public Update(String id, String json) {
        JSONObject obj = null;
        try {
            obj = new JSONObject(json);

            // Save ID
            this.id = id;

            // Note: When adding new fields, make sure to add them here!
            name = obj.getString(NAME);
            description = obj.getString(DESCRIPTION);
            datetime = obj.getString(DATETIME);
            avatarUrl = obj.getString(AVATAR_URL);

        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Update(String id, JSONObject obj) {
        try {

            // Save ID
            this.id = id;

            // Note: When adding new fields, make sure to add them here!
            name = obj.getString(NAME);
            description = obj.getString(DESCRIPTION);
            datetime = obj.getString(DATETIME);
            avatarUrl = obj.getString(AVATAR_URL);

        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<Update> loadDummyList() {
        ArrayList<Update> updates = new ArrayList<Update>();

        String[] descriptions = {
                "Who are you? How did I get here? What's going on?!",
                "Hackathon cancelled. Party time!!!",
                "Is this thing on?",
                "Food will be ready in a few minutes. It's time to take a break.",
                "Wifi is currently being interrupted by malicious attacks. Our secret agents are working to resolve the issue."
        };

        String[] names = {
                "Homeless man who hacked in",
                "Shane Creighton-Young",
                "Moez Bhatti",
                "Si Te Feng",
                "Kartik Talwar"
        };

        String[] dates = {
                "2014-08-06T19:30:11-04:00",
                "2014-08-06T18:30:11-04:00",
                "2014-08-06T17:30:11-04:00",
                "2014-08-06T15:30:11-04:00",
                "2014-06-21T19:30:11-04:00"
        };

        for (int i = 0; i < descriptions.length; i++) {
            Update update = new Update();
            update.description = descriptions[i];
            update.name = names[i];
            update.datetime = dates[i];
            updates.add(update);
        }

        return updates;
    }

    // This method takes a JSON string and returns an ArrayList of Update from
    // the JSON data.
    public static ArrayList<Update> loadUpdateArrayFromJSON(String json) {

        // Open the JSON object from string
        JSONObject obj;
        try {
            obj = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        ArrayList<Update> updates = new ArrayList<Update>(obj.length());

        // Note: The JSONObject API returns an Iterator<String>, so this is a
        // safe cast.
        @SuppressWarnings("unchecked")
        Iterator<String> it = obj.keys();

        while (it.hasNext()) {
            // Get the key from the JSON object.
            String id = it.next();

            JSONObject updateJSON;
            try {
                updateJSON = obj.getJSONObject(id);
            } catch (JSONException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            // Load the update from that object and add it to the result
            Update update = new Update(id, updateJSON);
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
}
