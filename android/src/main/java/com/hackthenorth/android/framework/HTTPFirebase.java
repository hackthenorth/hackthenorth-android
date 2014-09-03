package com.hackthenorth.android.framework;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An HTTP / Intent-based module for Firebase.
 * 
 * @author Shane Creighton-Young
 * 
 */
public class HTTPFirebase {

    protected static final String TAG = "HTTPFirebase";

    private static final boolean DEBUG = true;
    
    private static final String FIREBASE_URL = "https://hackthenorth.firebaseio.com/mobile";


    /**
     * @param path
     *            The path of the Firebase data to GET. Note: do not include '.json' at the end
     *            of this path!
     * @param context
     *            Application context for broadcasting the intent
     * @param action
     *            The action string which should be passed in the intent to be broadcast
     */
    public static void GET(final String path, final Context context,
                           final String action) {

        String url = String.format("%s/%s.json", FIREBASE_URL, path);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Broadcast the new data
                        Intent intent = new Intent(action);
                        intent.putExtra(action, response);

                        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
                        manager.sendBroadcast(intent);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.toString());
                        // TODO
                    }
                });

        NetworkManager.getRequestQueue().add(request);
    }

    /**
     *
     * @param path
     *            The path of the Firebase data to PATCH
     * @param data
     *            The data to PATCH to the Firebase path
     */
    public static void PATCH(final String path, final String data) {

        String url = String.format("%s/%s.json", FIREBASE_URL, path);
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "patch response: " + response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.toString());
                    }
                }
        ) {

            @Override
            public Map<String, String> getParams() {
                return Collections.emptyMap();
            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>(1);
                headers.put("X-HTTP-Method-Override", "PATCH");
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                try {
                    return data.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    return data.getBytes();
                }
            }
        };
        NetworkManager.getRequestQueue().add(request);
    }
}
