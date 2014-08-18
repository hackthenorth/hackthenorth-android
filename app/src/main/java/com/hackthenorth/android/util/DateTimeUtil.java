package com.hackthenorth.android.util;

import java.text.SimpleDateFormat;

public class DateTimeUtil {

    public static SimpleDateFormat getISO8601SimpleDateFormat() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-d'T'HH:mm:ssZZZZZ");
        return format;
    }
}
