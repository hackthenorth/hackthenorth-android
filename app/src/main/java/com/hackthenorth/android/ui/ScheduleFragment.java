package com.hackthenorth.android.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hackthenorth.android.HackTheNorthApplication;
import com.hackthenorth.android.R;
import com.hackthenorth.android.framework.HTNNotificationManager;
import com.hackthenorth.android.framework.HTTPFirebase;
import com.hackthenorth.android.model.ScheduleItem;
import com.hackthenorth.android.util.DateTimeUtil;

/**
 * A fragment for displaying lists of Update.
 */
public class ScheduleFragment extends Fragment {
    public static final String TAG = "UpdateListFragment";

    private ListView mListView;
    private ArrayList<ScheduleItem> mData;
    private ScheduleFragmentAdapter mAdapter;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Set up BroadcastReceiver for updates.
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (HackTheNorthApplication.Actions.SYNC_SCHEDULE
                        .equals(intent.getAction())) {

                    // Update with the new data
                    String key = intent.getAction();
                    String json = intent.getStringExtra(key);
                    onUpdate(json);
                }
            }
        };

        // Register our broadcast receiver.
        IntentFilter filter = new IntentFilter();
        filter.addAction(HackTheNorthApplication.Actions.SYNC_SCHEDULE);

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(activity);
        manager.registerReceiver(mBroadcastReceiver, filter);

        HTTPFirebase.GET("/schedule", activity,
                HackTheNorthApplication.Actions.SYNC_SCHEDULE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the view and return it
        View view = inflater.inflate(R.layout.schedule_fragment, container, false);

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
            HTTPFirebase.GET("/schedule", getActivity(),
                    HackTheNorthApplication.Actions.SYNC_SCHEDULE);
        }
    }

    // Receive a JSON update
    public void onUpdate(String json) {

        // Set or update our data
        if (mData == null) {
            // Returns a list of ScheduleItem sorted by start_time
            mData = ScheduleItem.loadScheduleFromJSON(json);

            // If we're ready, set up the adapter with the list.
            setupAdapterIfReady();

        } else {
            // TODO: Is JSON parsing fast enough to be on the main thread?
            ArrayList<ScheduleItem> newData = ScheduleItem.loadScheduleFromJSON(json);

            mData.clear();
            mData.addAll(newData);

            mAdapter.notifyDataSetChanged();
        }
    }

    private void setupAdapterIfReady() {
        // Only set up adapter if our ListView and our data are ready.
        if (mListView != null && mData != null) {

            // Create adapter
            mAdapter = new ScheduleFragmentAdapter(mListView.getContext(),
                    R.layout.schedule_list_item, mData);

            // Hook it up to the ListView
            mListView.setAdapter(mAdapter);
        }
    }

    public static class ScheduleFragmentAdapter extends ArrayAdapter<ScheduleItem> {
        private int mResource;
        private ArrayList<ScheduleItem> mData;

        public ScheduleFragmentAdapter(Context context, int resource,
                                       ArrayList<ScheduleItem> objects) {
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

            // Set up the click event
            final Intent intent = getIntent(position);
            final Context context = convertView.getContext();
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(intent);
                }
            });

            // Get the data for this position
            ScheduleItem scheduleItem = mData.get(position);

            ((TextView)convertView.findViewById(R.id.schedule_item_name))
                    .setText(scheduleItem.name);
            ((TextView)convertView.findViewById(R.id.schedule_item_description))
                    .setText(scheduleItem.description);

            return convertView;
        }

        public Intent getIntent(int position) {
            ScheduleItem item = mData.get(position);

            SimpleDateFormat format = DateTimeUtil.getISO8601SimpleDateFormat();
            Date start = null, end = null;
            try {
                start = format.parse(item.start_time);
                end = format.parse(item.end_time);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Intent result = null;
            if (start != null && end != null) {
                result = new Intent(Intent.ACTION_INSERT)
                        .setType("vnd.android.cursor.item/event")
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                start.getTime())
                        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                                end.getTime())
                        .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
                        .putExtra(Events.TITLE, item.name)
                        .putExtra(Events.DESCRIPTION, item.description)
                        .putExtra(Events.EVENT_LOCATION, item.location)
                        .putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY)
                        .putExtra(Events.ACCESS_LEVEL, Events.ACCESS_PRIVATE);
            }

            return result;
        }

        public int getCount() {
            return mData.size();
        }
    }
}
