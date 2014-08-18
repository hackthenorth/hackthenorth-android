package com.hackthenorth.android.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.hackthenorth.android.HackTheNorthApplication;
import com.hackthenorth.android.R;
import com.hackthenorth.android.framework.HTTPFirebase;
import com.hackthenorth.android.model.Prize;
import com.hackthenorth.android.ui.component.TextView;

import java.util.ArrayList;

public class PrizesFragment extends Fragment {
    public static final String TAG = "UpdateListFragment";

    private ListView mListView;
    private ArrayList<Prize> mData;
    private PrizesFragmentAdapter mAdapter;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Set up BroadcastReceiver for updates.
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (HackTheNorthApplication.Actions.SYNC_PRIZES
                        .equals(intent.getAction())) {

                    // Update with the new data
                    String key = HackTheNorthApplication.Actions.SYNC_PRIZES;
                    String json = intent.getStringExtra(key);
                    onUpdate(json);
                }
            }
        };

        // Register our broadcast receiver.
        IntentFilter filter = new IntentFilter();
        filter.addAction(HackTheNorthApplication.Actions.SYNC_PRIZES);

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(activity);
        manager.registerReceiver(mBroadcastReceiver, filter);

        HTTPFirebase.GET("/prizes", activity,
                HackTheNorthApplication.Actions.SYNC_PRIZES);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the view and return it
        View view = inflater.inflate(R.layout.prizes_fragment, container, false);

        // Save a reference to the list view
        mListView = (ListView) view.findViewById(android.R.id.list);

        // If we're ready, set up the adapter with the list now.
        setupAdapterIfReady();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Re-GET data from Firebase here
        if (getActivity() != null) {
            HTTPFirebase.GET("/prizes", getActivity(),
                    HackTheNorthApplication.Actions.SYNC_PRIZES);
        }
    }

    // Receive a JSON update
    public void onUpdate(String json) {

        // Set or update our data
        if (mData == null) {
            mData = Prize.loadPrizesFromJSON(json);

            // If we're ready, set up the adapter with the list.
            setupAdapterIfReady();

        } else {
            // TODO: Is JSON parsing fast enough to be on the main thread?
            ArrayList<Prize> newData = Prize.loadPrizesFromJSON(json);

            mData.clear();
            mData.addAll(newData);

            mAdapter.notifyDataSetChanged();
        }
    }

    private void setupAdapterIfReady() {
        // Only set up adapter if our ListView and our data are ready.
        if (mListView != null && mData != null) {

            // Create adapter
            mAdapter = new PrizesFragmentAdapter(mListView.getContext(),
                    R.layout.prizes_list_item, mData);

            // Hook it up to the ListView
            mListView.setAdapter(mAdapter);
        }
    }

    public static class PrizesFragmentAdapter extends ArrayAdapter<Prize> {
        private int mResource;
        private ArrayList<Prize> mData;

        public PrizesFragmentAdapter(Context context, int resource,
                                     ArrayList<Prize> objects) {
            super(context, resource, objects);

            mResource = resource;
            mData = objects;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                // If we don't have a view to reuse, inflate a new one.
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(mResource, parent, false);
            }

            // Get the data for this position
            Prize prize = mData.get(position);

            ((TextView)convertView.findViewById(R.id.prize_name))
                    .setText(prize.name);
            ((TextView)convertView.findViewById(R.id.prize_description))
                    .setText(prize.description);

            return convertView;
        }

        public int getCount() {
            return mData.size();
        }
    }
}
