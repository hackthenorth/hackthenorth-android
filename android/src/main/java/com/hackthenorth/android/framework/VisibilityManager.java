package com.hackthenorth.android.framework;

public class VisibilityManager {

    // True if one of our activities are visible, false otherwise.
    private static boolean mActivityVisible;


    public static void activityResumed() { mActivityVisible = true; }
    public static void activityPaused() { mActivityVisible = false; }
    public static boolean isActivityVisible() { return mActivityVisible; }
}
