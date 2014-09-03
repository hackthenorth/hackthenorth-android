package com.hackthenorth.android.framework;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Keeps track of global network queues and caches. Implemented according to Volley
 * API guide at [1].
 *
 * [1]: http://developer.android.com/training/volley/requestqueue.html#singleton
 */
public class NetworkManager {

    private static RequestQueue mRequestQueue;
    private static ImageLoader mImageLoader;
    private static Context mContext;

    public static void initialize(Context context) {
        mContext = context;

        // Initialize request queue
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }

        // Initialize image loader
        mImageLoader = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(20);

            @Override
            public Bitmap getBitmap(String s) {
                return cache.get(s);
            }

            @Override
            public void putBitmap(String s, Bitmap bitmap) {
                cache.put(s, bitmap);
            }
        });
    }

    public static ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public static RequestQueue getRequestQueue() {
        return mRequestQueue;
    }
}
