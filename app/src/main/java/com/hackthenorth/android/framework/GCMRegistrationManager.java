package com.hackthenorth.android.framework;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

public class GCMRegistrationManager {

    static private final String CLASS_NAME = "GCMRegistrationManager";

    // This is the key which we use to store the registration ID and the app
    // version in SharedPreferences.
    static private final String REG_ID = "registrationId";
    static private final String APP_VERSION = "appVersion";

    // I (Shane) registered for GCM with my developer account, and got this
    // number from Google's API Console. I don't think it's a secret value.
    static private final String SENDER_ID = "833213383561";

    public static void registerInBackground(final Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
                    String regid = gcm.register(SENDER_ID);

                    Log.i("GCMRegistrationManager", "new regid: " + regid);

                    // Update the registration ID in Firebase.
                    sendRegistrationIdToFirebase(context, regid);

                    // IMPORTANT NOTE: sendRegistrationIdToFirebase uses the old
                    // registration ID in SharedPreferences to do some important
                    // bookkeeping. This method must run *after*
                    // sendRegistrationIdToFirebase.
                    storeRegistrationId(context, regid);

                } catch (IOException ex) {
                    // TODO: Handle errors.

                    // This is important, because I imagine GCM uses the internet
                    // to do registration, and the request might timeout if internet
                    // isn't perfect.
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void nothing) {
            }
        }.execute(null, null, null);
    }

    /**
     * @return The apps GCM registration ID, or null if we haven't registered yet.
     */
    public static String getRegistrationId(Context context) {

        final SharedPreferences preferences =
                context.getSharedPreferences(CLASS_NAME, Context.MODE_PRIVATE);
        String id = preferences.getString(REG_ID, null);

        // If id isn't null, we need to check if the app was updated. This is
        // because the saved registration ID isn't guaranteed to work if we've
        // updated the app.
        if (id != null) {
            int registeredVersion = preferences.getInt(APP_VERSION, Integer.MIN_VALUE);
            int currentVersion = getAppVersion(context);

            if (registeredVersion != currentVersion) {
                id = null;
            }
            // Otherwise, we're safe to use the old registration ID.
        }

        return null;
    }

    /**
     * @return The app's version code from the PackageManager.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // Should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Updates the registration ID in Firebase. If we had an old registration
     * ID for this device, we remove it.
     */
    private static void sendRegistrationIdToFirebase(Context context, String regid) {
        HTTPFirebase.PUT(String.format("/notifications/android/%s", regid),
                "\"dummy\"", null);
    }

    /**
     * Stores the registration ID to shared preferences.
     */
    private static void storeRegistrationId(Context context, String id) {
        final SharedPreferences.Editor editor =
                context.getSharedPreferences(CLASS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(REG_ID, id);
        editor.commit();
    }
}
