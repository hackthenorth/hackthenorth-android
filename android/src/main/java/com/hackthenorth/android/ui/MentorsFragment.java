package com.hackthenorth.android.ui;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.hackthenorth.android.HackTheNorthApplication;
import com.hackthenorth.android.R;
import com.hackthenorth.android.base.BaseListFragment;
import com.hackthenorth.android.framework.HTTPFirebase;
import com.hackthenorth.android.framework.NetworkManager;
import com.hackthenorth.android.model.Mentor;
import com.hackthenorth.android.util.DateFormatter;

import java.util.ArrayList;

public class MentorsFragment extends BaseListFragment {
    private final String TAG = "MentorsFragment";

    private ListView mListView;
    private ArrayList<Mentor> mData = new ArrayList<Mentor>();
    private MentorListAdapter mAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Create adapter
        mAdapter = new MentorListAdapter(activity, R.layout.mentor_list_item, mData);

        // Register for updates
        registerForSync(activity, HackTheNorthApplication.Actions.SYNC_MENTORS, mAdapter);

        HTTPFirebase.GET("/mentors", activity, HackTheNorthApplication.Actions.SYNC_MENTORS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the view and return it
        View view = inflater.inflate(R.layout.mentors_fragment, container, false);

        // Set up list
        mListView = (ListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);

        // Hook up activity to fragment so it knows when to dismiss the search box
        if (getActivity() instanceof AbsListView.OnScrollListener) {
            AbsListView.OnScrollListener l = (AbsListView.OnScrollListener)getActivity();
            mListView.setOnScrollListener(l);
        }

        return view;
    }

    @Override
    protected void handleJSONUpdateInBackground(final String json) {
        final Activity activity = getActivity();
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... nothing) {

                // Decode JSON
                final ArrayList<Mentor> newData = Mentor.loadMentorArrayFromJSON(json);

                if (activity != null && mAdapter != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Copy the data into the ListView on the main thread and
                            // refresh.
                            mData.clear();
                            mData.addAll(newData);
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                }

                return null;
            }
        }.execute();
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
                availability += DateFormatter.getTimespanString(timeslots.get(i));

                if (i != timeslots.size() - 1) {
                    availability += "\n";
                }
            }

            return availability;
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
}
