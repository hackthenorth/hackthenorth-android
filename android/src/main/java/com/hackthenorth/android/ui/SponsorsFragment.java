package com.hackthenorth.android.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.NetworkImageView;
import com.hackthenorth.android.HackTheNorthApplication;
import com.hackthenorth.android.R;
import com.hackthenorth.android.framework.HTTPFirebase;
import com.hackthenorth.android.framework.NetworkManager;

public class SponsorsFragment extends Fragment {

    private static final String TAG = "SponsorsFragment";

    private BroadcastReceiver mReceiver;

    private NetworkImageView mImage;
    private String mUrl;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (mReceiver == null) {
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String response = intent.getStringExtra(
                            HackTheNorthApplication.Actions.SYNC_SPONSORS);
                    // strip the quotes from the response (it's a JSON string)
                    mUrl = response.substring(1, response.length()-1);
                    setImageUrlWhenReady();
                }
            };

            // Register our broadcast receiver.
            IntentFilter filter = new IntentFilter();
            filter.addAction(HackTheNorthApplication.Actions.SYNC_SPONSORS);

            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(activity);
            manager.registerReceiver(mReceiver, filter);
        }

        HTTPFirebase.GET("/sponsors", activity, HackTheNorthApplication.Actions.SYNC_SPONSORS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the view and return it
        View view = inflater.inflate(R.layout.sponsors_fragment, container, false);

        mImage = (NetworkImageView)view.findViewById(R.id.image);
        setImageUrlWhenReady();

        return view;
    }

    private void setImageUrlWhenReady() {
        if (mUrl != null && mImage != null) {
            mImage.setImageUrl(mUrl, NetworkManager.getImageLoader());
        }
    }
}
