package com.hackthenorth.android.framework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;

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
                
                // TODO: validation?
                // Note: Firebase seems to be pretty good at doing sane things,
                // even when your
                // paths suck. i.e. https://foo.firebaseio.com///////data/.json
                // works fine.
                String url = String.format("https://%s.firebaseio.com/%s.json", FIREBASE_ID, path);
                return GET(url);
            }
            
            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                }
            }
        }.execute(path);
    }
    
    /**
     * 
     * @param url
     *            The url to GET
     * @return The string contents of the response, or null if error.
     */
    private static String GET(String url) {
        String result = null;
        
        try {
            // Make the HTTP request
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
            InputStream inputStream = httpResponse.getEntity().getContent();
            
            // Convert inputstream to string
            if (inputStream != null) {
                result = convertInputStreamToString(inputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        
        // Reads the input stream to a string line by line
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = bufferedReader.readLine();
        String result = "";
        while (line != null) {
            result += line;
            line = bufferedReader.readLine();
        }
        inputStream.close();
        return result;
    }
}
