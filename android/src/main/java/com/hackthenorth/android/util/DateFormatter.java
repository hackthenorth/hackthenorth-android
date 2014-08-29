package com.hackthenorth.android.util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DateFormatter {
    private static final String TAG = "DateTileUtil";

    public static SimpleDateFormat getISO8601SimpleDateFormat() {
        return new SimpleDateFormat("yyyy-MM-d'T'HH:mm:ssZZZZZ");
    }

    public static String getTimespanString(ArrayList<String> timeslotTimes) {
        // Make sure there is and only is a start time and an end time for each timeslot array
        if (timeslotTimes.size() != 2) {
            Log.e(TAG, "Invalid availability");
            return null;
        }

        String timespan = "";

        Timeslot start = buildTimeslot(timeslotTimes.get(0));
        Timeslot end = buildTimeslot(timeslotTimes.get(1));

        if (start.day.equals(end.day)) {

            // If both times are on the same day, don't write the day twice
            timespan += start.day + " ";

            if (start.period.equals(end.period)) {
                // If both times are in the same period, only write the period at the end
                timespan += start.hour + ":" + start.minute +
                        " - " + end.hour + ":" + end.minute + end.period;
            } else {
                // Both times have different periods, write each one
                timespan += start.hour + ":" + start.minute + start.period +
                        " - " + end.hour + ":" + end.minute + end.period;
            }
        } else {
            // Both times are on different days, write each one
            timespan += start.day + " " + start.hour + ":" + start.minute + start.period +
                    " - " + end.day + " " + end.hour + ":" + end.minute + end.period;
        }

        return timespan;
    }

    private static Timeslot buildTimeslot(String s) {

        Date date = new Date();
        try {
            date = getISO8601SimpleDateFormat().parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Timeslot timeslot = new Timeslot();
        timeslot.day = new SimpleDateFormat("EEEE").format(date);
        timeslot.hour = new SimpleDateFormat("h").format(date);
        timeslot.minute = new SimpleDateFormat("mm").format(date);
        timeslot.period = new SimpleDateFormat("a").format(date);

        return timeslot;
    }

    /**
     * Small data class to hold the parts of the timestamp for easy comparisons
     */
    private static class Timeslot {
        String day;
        String hour;
        String minute;
        String period;
    }
}
