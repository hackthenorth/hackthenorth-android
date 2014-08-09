package com.hackthenorth.android.framework;

import android.os.AsyncTask;
import android.util.Log;

/**
 * An HTTP module for Firebase.
 * 
 * Fun fact: Firebase doesn't put any limits on the number of GET requests you
 * issue! This is why we're using the REST API instead of the realtime API,
 * which can be throttled and is generally unscalable.
 * 
 * @author Shane Creighton-Young
 * 
 */
public class HTTPFirebase {
    
    private static final String FIREBASE_ID = "shane-hackthenorth";
    protected static final String TAG = "HTTPFirebase";
    
    public static interface Callback {
        public void onSuccess(String json);
        public void onError();
    }
    
    /**
     * 
     * @param path
     *            The path of the Firebase data to GET
     * @param callback
     *            The callback for when the data is returned
     */
    public static void GET(String path, final Callback callback) {
        // Start an AsyncTask to GET the string
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... strings) {
                // Specify the path of the firebase data
                String path = strings[0];
                
                String url = String.format("https://%s.firebaseio.com/%s.json", FIREBASE_ID, path);
                return HTTPRequests.GET(url);
            }
            
            @Override
            protected void onPostExecute(String result) {
                if (result != null && callback != null) {
                    callback.onSuccess(result);
                }
            }
        }.execute(path);
    }

    /**
     *
     * @param path
     *            The path of the Firebase data to PUT
     * @param data
     *            The data to push to the Firebase ref
     * @param callback
     *            The callback for when the data is returned
     */
    public static void PUT(String path, String data, final Callback callback) {

        Log.d(TAG, String.format("Issuing PUT request to %s with data %s", path, data));

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
                if (result != null && callback != null) {
                    callback.onSuccess(result);
                }
            }
        }.execute(path, data);
    }
}
