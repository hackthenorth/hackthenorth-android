package com.hackthenorth.android.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
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
import com.hackthenorth.android.model.TeamMember;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class TeamFragment extends Fragment {
    public static final String TAG = "TeamFragment";

    // Argument keys
    public static final String DATA_ID = "mDataID";

    private ListView mListView;
    private ArrayList<TeamMember> mData;
    private InfoListAdapter mAdapter;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Extract data from arguments
        Bundle args = getArguments();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Set up BroadcastReceiver for updates.
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (HackTheNorthApplication.Actions.SYNC_UPDATES .equals(intent.getAction())) {

                    // Forward to fragment
                    String key = HackTheNorthApplication.Actions.SYNC_UPDATES;
                    String json = intent.getStringExtra(key);

                    onUpdate(json);

                    // else if other kind of fragment update, etc.
                }
            }
        };

        // Register our broadcast receiver.
        IntentFilter filter = new IntentFilter();
        filter.addAction(HackTheNorthApplication.Actions.SYNC_UPDATES);

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(activity);
        manager.registerReceiver(mBroadcastReceiver, filter);

        HTTPFirebase.GET("/team", activity, HackTheNorthApplication.Actions.SYNC_UPDATES);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the view and return it
        View view = inflater.inflate(R.layout.team_list_fragment, container, false);

        // Save a reference to the list view
        mListView = (ListView) view.findViewById(android.R.id.list);

        // If we're ready, set up the adapter with the list now.
        setupAdapterIfReady();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    // Receive a JSON team member
    public void onUpdate(String json) {

        // Set or update our data
        if (mData == null) {
            mData = TeamMember.loadTeamMemberArrayFromJSON(json);

            // If we're ready, set up the adapter with the list.
            setupAdapterIfReady();

        } else {
            // TODO: Is JSON parsing fast enough to be on the main thread?
            ArrayList<TeamMember> newData = TeamMember.loadTeamMemberArrayFromJSON(json);

            mData.clear();
            mData.addAll(newData);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void setupAdapterIfReady() {
        // Only set up adapter if our ListView and our data are ready.
        if (mListView != null && mData != null) {

            // Create adapter
            mAdapter = new InfoListAdapter(mListView.getContext(), R.layout.team_list_item, mData);

            // Hook it up to the ListView
            mListView.setAdapter(mAdapter);
        }
    }

    public static class InfoListAdapter extends ArrayAdapter<TeamMember> {
        private int mResource;
        private ArrayList<TeamMember> mData;

        public InfoListAdapter(Context context, int resource, ArrayList<TeamMember> objects) {
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
            TeamMember teamMember = mData.get(position);

            // Set up the image view with the avatar URLs
            NetworkImageView networkImageView = (NetworkImageView) convertView.findViewById(R.id.team_member_avatar);
            networkImageView.setDefaultImageResId(R.drawable.ic_launcher);

            // If we have an avatar URL, load it here.
            ImageLoader loader = NetworkManager.getImageLoader();
            if (!"".equals(teamMember.avatar)) {
                networkImageView.setImageUrl(teamMember.avatar, loader);
            } else {
                networkImageView.setImageUrl(null, loader);
            }

            // Set the data in the TextViews
            ((TextView) convertView.findViewById(R.id.team_member_name)).setText(teamMember.name);
            ((TextView) convertView.findViewById(R.id.team_member_role)).setText(getRolesString(teamMember.role));

            return convertView;
        }

        public int getCount() {
            return mData.size();
        }

        private String getRelativeTimestamp(String s) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-d'T'HH:mm:ssZZZZZ");

            long date = 0;
            try {
                date = formatter.parse(s).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String relativeTimestamp = (String) DateUtils.getRelativeTimeSpanString(date);
            if (relativeTimestamp.equals("in 0 minutes") || relativeTimestamp.equals("0 minutes ago"))
                return "Just now";
            return relativeTimestamp;
        }

        private String getRolesString(ArrayList<String> rolesList) {

            if (rolesList == null || rolesList.size() == 0) {
                return null;
            }

            String roles = rolesList.get(0);

            for (int i = 1; i < rolesList.size(); i++) {
                roles += " • " + rolesList.get(i);
            }

            return roles;
        }
    }
}
