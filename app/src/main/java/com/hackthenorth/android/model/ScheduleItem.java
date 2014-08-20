package com.hackthenorth.android.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hackthenorth.android.util.DateTimeUtil;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ScheduleItem {

    private static final String TAG = "Update";
    public String id;

    public String type;

    public String description;
    public String location;
    public String name;
    public String speaker;

    public String start_time;
    public String end_time;

    // This method takes a JSON string and returns an ArrayList of ScheduleItem from
    // the JSON data.
    public static ArrayList<ScheduleItem> loadScheduleFromJSON(String json) {

        // Deserialize using GSON.
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, ScheduleItem>>(){}.getType();
        HashMap<String, ScheduleItem> scheduleItemMap = gson.fromJson(json, type);

        // Build an ArrayList of schedule items from the hashmap.
        ArrayList<ScheduleItem> scheduleItems =
                new ArrayList<ScheduleItem>(scheduleItemMap.size());
        for (Map.Entry<String, ScheduleItem> entry : scheduleItemMap.entrySet()) {
            ScheduleItem scheduleItem = entry.getValue();
            scheduleItem.id = entry.getKey();
            scheduleItems.add(scheduleItem);
        }

        // Sort the list
        Collections.sort(scheduleItems, new Comparator<ScheduleItem>() {
            @Override
            public int compare(ScheduleItem lhs, ScheduleItem rhs) {
                // Sort the schedule items by start_time
                SimpleDateFormat format = DateTimeUtil.getISO8601SimpleDateFormat();
                Date lhsStartTime = null;
                try {
                    lhsStartTime = format.parse(lhs.start_time);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Date rhsStartTime = null;
                try {
                    rhsStartTime = format.parse(rhs.start_time);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (lhsStartTime == null) {
                    return -1;
                } else {
                    return lhsStartTime.compareTo(rhsStartTime);
                }
            }
        });

        return scheduleItems;
    }
}
