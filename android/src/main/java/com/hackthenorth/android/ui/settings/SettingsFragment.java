package com.hackthenorth.android.ui.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.hackthenorth.android.R;

public class SettingsFragment extends PreferenceFragment {

    public static final String PREF_NOTIFS =
            "PREFERENCE_NOTIFICATIONS";
    public static final String PREF_NOTIF_RINGTONE =
            "PREFERENCE_NOTIFICATION_RINGTONE";
    public static final String PREF_NOTIF_VIBRATE =
            "PREFERENCE_NOTIFICATION_VIBRATE";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

}
