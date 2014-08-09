package com.hackthenorth.android.framework;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * The state of HTTP APIs in Java <<<<<<<<<<<<<<<
 */
public class HTTPRequests {

    private static final String TAG = "HTTPRequests";

    /**
     * @param url
     *            The url to GET
     * @return The string contents of the response, or null if error.
     */
    public static String GET(String url) {
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

    /**
     * @param url
     *            The url to POST
     * @param data
     *            The data to attach to the post
     * @return The string contents of the response, or null if error.
     */
    public static String PUT(String url, String data) {

        Log.d(TAG, String.format("Issuing PUT request to %s with data %s", url, data));

        String result = null;
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPut put = new HttpPut(url);

            // Request parameters and other properties.
            put.setEntity(new StringEntity(data, "UTF-8"));

            // Execute and get the response.
            HttpResponse response = httpclient.execute(put);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                InputStream instream = entity.getContent();
                try {
                    result = convertInputStreamToString(instream);
                } finally {
                    instream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "result: " + result);
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
