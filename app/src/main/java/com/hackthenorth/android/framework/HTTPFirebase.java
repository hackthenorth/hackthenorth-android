package com.hackthenorth.android.framework;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

/**
 * An HTTP / Intent-based module for Firebase.
 * 
 * @author Shane Creighton-Young
 * 
 */
public class HTTPFirebase {
    
    private static final String FIREBASE_ID = "shane-hackthenorth";
    private static final String FIREBASE_CACHE_KEY = "FIREBASE_CACHE";

    protected static final String TAG = "HTTPFirebase";

    /**
     * @param path
     *            The path of the Firebase data to GET
     * @param context
     *            Application context for broadcasting the intent
     * @param action
     *            The action string which should be passed in the intent to be broadcast
     */
    public static void GET(final String path, final Context context,
                           final String action) {

        // First, broadcast any cached data that we have.
        String cached = getCachedJSON(context, path);
        if (cached != null) {
            Intent intent = new Intent(action);
            intent.putExtra(action, cached);

            LocalBroadcastManager manager =
                    LocalBroadcastManager.getInstance(context);
            manager.sendBroadcast(intent);
        }

        // Start an AsyncTask to GET the string
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... nothingness) {
                String url = String.format("https://%s.firebaseio.com/%s.json",
                        FIREBASE_ID, path);
                return HTTPRequests.GET(url);
            }
            
            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    // Put the data into the cache
                    setCachedJSON(context, path, result);

                    // Broadcast the new data
                    Intent intent = new Intent(action);
                    intent.putExtra(action, result);

                    LocalBroadcastManager manager =
                            LocalBroadcastManager.getInstance(context);
                    manager.sendBroadcast(intent);
                }
            }
        }.execute();
    }

    /**
     *
     * @param path
     *            The path of the Firebase data to PUT
     * @param data
     *            The data to push to the Firebase ref
     */
    public static void PUT(String path, String data) {

        // Start an AsyncTask to PUT the string
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... strings) {
                // Specify the path of the firebase data
                String path = strings[0];
                String data = strings[1];

                String url = String.format("https://%s.firebaseio.com/%s.json",
                        FIREBASE_ID, path);
                return HTTPRequests.PUT(url, data);
            }

            @Override
            protected void onPostExecute(String result) {
            }
        }.execute(path, data);
    }

    private static String getCachedJSON(Context context, String path) {
        SharedPreferences preferences = context.getSharedPreferences(FIREBASE_CACHE_KEY,
                Context.MODE_PRIVATE);
        return preferences.getString(path, null);
    }

    private static void setCachedJSON(Context context, String path, String json) {
        SharedPreferences.Editor editor = context.getSharedPreferences(FIREBASE_CACHE_KEY,
                Context.MODE_PRIVATE).edit();
        editor.putString(path, json);
        editor.commit();
    }
}
