package com.hackthenorth.android.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.hackthenorth.android.HackTheNorthApplication;
import com.hackthenorth.android.R;
import com.hackthenorth.android.base.BaseListFragment;
import com.hackthenorth.android.framework.HTTPFirebase;
import com.hackthenorth.android.framework.NetworkManager;
import com.hackthenorth.android.model.TeamMember;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class TeamFragment extends BaseListFragment {
    public static final String TAG = "TeamFragment";

    private ListView mListView;
    private ArrayList<TeamMember> mData = new ArrayList<TeamMember>();
    private TeamFragmentAdapter mAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Keep a static cache of the arraylist, because decoding from JSON every time is
        // a waste.
        String key = ViewPagerAdapter.TEAM_TAG;
        Object thing = getCachedObject(key);
        if (thing != null) {
            mData = (ArrayList<TeamMember>)thing;
        } else {
            setCachedObject(key, mData);
        }

        // Create adapter
        mAdapter = new TeamFragmentAdapter(activity, R.layout.team_list_item, mData);

        // Register for updates
        registerForSync(activity, HackTheNorthApplication.Actions.SYNC_TEAM);

        HTTPFirebase.GET("/team", activity, HackTheNorthApplication.Actions.SYNC_TEAM);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the view and return it
        View view = inflater.inflate(R.layout.list_fragment, container, false);

        // Save a reference to the list view
        mListView = (ListView) view.findViewById(android.R.id.list);
        mListView.setFastScrollEnabled(true);

        // Hook it up to the ListView
        mListView.setAdapter(mAdapter);

        return view;
    }

    @Override
    protected void handleJSONUpdateInBackground(final String json, String action) {
        final Activity activity = getActivity();
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... nothing) {

                // Decode JSON
                final ArrayList<TeamMember> newData =
                        TeamMember.loadTeamMemberArrayFromJSON(json);

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

    public static class TeamFragmentAdapter extends ArrayAdapter<TeamMember> implements SectionIndexer {

        private final String sections = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        private int mResource;
        private ArrayList<TeamMember> mData;

        public TeamFragmentAdapter(Context context, int resource, ArrayList<TeamMember> objects) {
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

        @Override
        public Object[] getSections() {

            String[] sectionsArr = new String[sections.length()];
            for (int i = 0; i < sections.length(); i++) {
                sectionsArr[i] = "" + sections.charAt(i);
            }

            return sectionsArr;
        }

        @Override
        public int getPositionForSection(int section) {

            for (int i = 0; i < getCount(); i++) {
                if (getItem(i).name.charAt(0) == sections.charAt(section)) {
                    return i;
                }
            }

            return 0;
        }

        @Override
        public int getSectionForPosition(int position) {

            char c = getItem(position).name.toUpperCase().charAt(0);
            int index = sections.indexOf(c);

            return index > 0 ? index : 0;
        }
    }
}
