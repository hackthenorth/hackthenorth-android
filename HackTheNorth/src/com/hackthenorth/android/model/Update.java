package com.hackthenorth.android.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Update {
	// Fields
	public String title;
	public String body;
	
	// JSON keys for fields
	public static final String TITLE = "title";
	public static final String BODY = "body";
	
	public Update(String json) {
		JSONObject obj = null;
		try {
			obj = new JSONObject(json);
			
			// Note: When adding new fields, make sure to add them here!
            title = obj.getString(TITLE);
            body = obj.getString(BODY);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public Update(JSONObject obj) {
		try {
			// Note: When adding new fields, make sure to add them here!
            title = obj.getString(TITLE);
            body = obj.getString(BODY);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		try {
			// Note: When adding new fields, make sure to add them here!
			obj.put(BODY, body);
			obj.put(TITLE, title);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return obj;
	}
	
	public static ArrayList<Update> loadUpdateArrayFromJSON(String json) {
		
		// Open the JSON array from string
		JSONArray arr;
		try {
			arr = new JSONArray(json);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		ArrayList<Update> updates = new ArrayList<Update>(arr.length());
		for (int i = 0; i < arr.length(); i++) {
			// Get the ith JSON object out of the array
			JSONObject obj;
			try {
				obj = arr.getJSONObject(i);
			} catch (JSONException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			
			// Load the update from that object and add it to the result
			Update update = new Update(obj);
			updates.add(update);
		}
		
		return updates;
	}
}
