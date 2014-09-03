package com.hackthenorth.android.model;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.hackthenorth.android.util.DateFormatter;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ScheduleItem extends Model implements Comparable<ScheduleItem> {

    private static final String TAG = "ScheduleItem";

    public static final String TYPE_EVENT = "event";
    public static final String TYPE_WORKSHOP = "workshop";
    public static final String TYPE_TALK = "talk";
    public static final String TYPE_SPEAKER = "speaker";
    public static final String TYPE_UPDATE = "update";
    public static final String TYPE_FOOD = "food";

    public String type;

    public String description;
    public String location;
    public String name;
    public String speaker;

    public String start_time;
    public String end_time;

    @Override
    public int compareTo(@NonNull ScheduleItem another) {

        // Sort the schedule items by start_time
        SimpleDateFormat format = DateFormatter.getISO8601SimpleDateFormat();
        Date lhsStartTime = null;
        try {
            lhsStartTime = format.parse(start_time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date rhsStartTime = null;
        try {
            rhsStartTime = format.parse(another.start_time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (lhsStartTime == null) {
            return -1;
        } else {
            return lhsStartTime.compareTo(rhsStartTime);
        }
    }
}
