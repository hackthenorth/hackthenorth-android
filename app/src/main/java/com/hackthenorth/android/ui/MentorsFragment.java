package com.hackthenorth.android.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.hackthenorth.android.HackTheNorthApplication;
import com.hackthenorth.android.R;
import com.hackthenorth.android.framework.HTTPFirebase;
import com.hackthenorth.android.framework.NetworkManager;
import com.hackthenorth.android.model.Mentor;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MentorsFragment extends Fragment {
    private final String TAG = "MentorsFragment";

    private ListView mListView;
    private ArrayList<Mentor> mData;
    private MentorListAdapter mAdapter;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Set up BroadcastReceiver for updates.
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (HackTheNorthApplication.Actions.SYNC_MENTORS .equals(intent.getAction())) {

                    // Forward to fragment
                    String key = HackTheNorthApplication.Actions.SYNC_MENTORS;
                    String json = intent.getStringExtra(key);

                    onUpdate(json);

                    // else if other kind of fragment update, etc.
                }
            }
        };

        // Register our broadcast receiver.
        IntentFilter filter = new IntentFilter();
        filter.addAction(HackTheNorthApplication.Actions.SYNC_MENTORS);

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(activity);
        manager.registerReceiver(mBroadcastReceiver, filter);

        HTTPFirebase.GET("/mentors", activity, HackTheNorthApplication.Actions.SYNC_MENTORS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the view and return it
        View view = inflater.inflate(R.layout.mentors_fragment, container, false);

        // Save a reference to the list view
        mListView = (ListView) view.findViewById(android.R.id.list);

        // If we're ready, set up the adapter with the list now.
        setupAdapterIfReady();

        return view;
    }

    // Receive a JSON update
    public void onUpdate(String json) {

        // Set or update our data
        if (mData == null) {
            mData = Mentor.loadMentorArrayFromJSON(json);

            // If we're ready, set up the adapter with the list.
            setupAdapterIfReady();

        } else {
            // TODO: Is JSON parsing fast enough to be on the main thread?
            ArrayList<Mentor> newData = Mentor.loadMentorArrayFromJSON(json);

            mData.clear();
            mData.addAll(newData);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void setupAdapterIfReady() {
        // Only set up adapter if our ListView and our data are ready.
        if (mListView != null && mData != null) {

            // Create adapter
            mAdapter = new MentorListAdapter(mListView.getContext(), R.layout.mentor_list_item, mData);

            // Hook it up to the ListView
            mListView.setAdapter(mAdapter);
        }
    }

    public static class MentorListAdapter extends ArrayAdapter<Mentor> {
        private final String TAG = "MentorListAdapter";

        private int mResource;
        private ArrayList<Mentor> mData;

        public MentorListAdapter(Context context, int resource, ArrayList<Mentor> objects) {
            super(context, resource, objects);

            mResource = resource;
            mData = objects;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                // If we don't have a view to reuse, inflate a new one.
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(mResource, parent, false);
            }

            // Get the data for this position
            Mentor mentor = mData.get(position);

            // Set up the image view with the avatar URLs
            NetworkImageView networkImageView = (NetworkImageView) convertView.findViewById(R.id.mentor_image);
            networkImageView.setDefaultImageResId(R.drawable.ic_launcher);

            // If we have an avatar URL, load it here.
            ImageLoader loader = NetworkManager.getImageLoader();
            if (!TextUtils.isEmpty(mentor.image)) {
                networkImageView.setVisibility(View.VISIBLE);
                networkImageView.setImageUrl(mentor.image, loader);
            } else {
                networkImageView.setVisibility(View.GONE);
            }

            // Set the data in the TextViews
            ((TextView) convertView.findViewById(R.id.mentor_name)).setText(mentor.name);
            ((TextView) convertView.findViewById(R.id.mentor_organization)).setText(mentor.organization);
            ((TextView) convertView.findViewById(R.id.mentor_availability)).setText(getAvailabilityString(mentor.availability));
            ((TextView) convertView.findViewById(R.id.mentor_skills)).setText(getSkillsString(mentor.skills));

            return convertView;
        }

        public int getCount() {
            return mData.size();
        }

        private String getAvailabilityString(ArrayList<ArrayList<String>> timeslots) {

            if (timeslots == null || timeslots.size() == 0) {
                return null;
            }

            String availability = "";

            for (int i = 0; i < timeslots.size(); i++) {
                ArrayList<String> timeslotTimes = timeslots.get(i);

                // Make sure there is and only is a start time and an end time for each timeslot array
                if (timeslotTimes.size() != 2) {
                    Log.e(TAG, "Invalid availability");
                    return null;
                }

                Timeslot start = buildTimeslot(timeslotTimes.get(0));
                Timeslot end = buildTimeslot(timeslotTimes.get(1));

                if (start.day.equals(end.day)) {

                    // If both times are on the same day, don't write the day twice
                    availability += start.day + ", ";

                    if (start.period.equals(end.period)) {
                        // If both times are in the same period, only write the period at the end
                        availability += start.hour + ":" + start.minute +
                                " to " + end.hour + ":" + end.minute + end.period;
                    } else {
                        // Both times have different periods, write each one
                        availability += start.hour + ":" + start.minute + start.period +
                                " to " + end.hour + ":" + end.minute + end.period;
                    }
                } else {
                    // Both times are on different days, write each one
                    availability += start.day + ", " + start.hour + ":" + start.minute + start.period +
                            " to " + end.day + ", " + end.hour + ":" + end.minute + end.period;
                }

                if (i != timeslots.size() - 1) {
                    availability += "\n";
                }
            }

            return availability;
        }

        private Timeslot buildTimeslot(String s) {

            Date date = new Date();
            try {
                date = new SimpleDateFormat("yyyy-MM-d'T'HH:mm:ssZZZZZ").parse(s);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Timeslot timeslot = new Timeslot();
            timeslot.day = new SimpleDateFormat("EEEE").format(date);
            timeslot.hour = new SimpleDateFormat("h").format(date);
            timeslot.minute = new SimpleDateFormat("mm").format(date);
            timeslot.period = new SimpleDateFormat("a").format(date);

            return timeslot;
        }

        private String getSkillsString(ArrayList<String> skillsList) {

            if (skillsList == null || skillsList.size() == 0) {
                return null;
            }

            String skills = skillsList.get(0);

            for (int i = 1; i < skillsList.size(); i++) {
                skills += " • " + skillsList.get(i);
            }

            return skills;
        }
    }

    /**
     * Small data class to hold the parts of the timestamp for easy comparisons
     */
    public static class Timeslot {
        String day;
        String hour;
        String minute;
        String period;
    }
}
