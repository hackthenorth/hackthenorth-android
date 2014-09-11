package com.hackthenorth.android.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.gson.reflect.TypeToken;
import com.hackthenorth.android.HackTheNorthApplication;
import com.hackthenorth.android.R;
import com.hackthenorth.android.base.BaseListFragment;
import com.hackthenorth.android.framework.HTTPFirebase;
import com.hackthenorth.android.framework.NetworkManager;
import com.hackthenorth.android.model.TeamMember;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class TeamFragment extends BaseListFragment {
    public static final String TAG = "TeamFragment";

    private ListView mListView;
    private ArrayList<TeamMember> mData = new ArrayList<TeamMember>();
    private TeamFragmentAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep a static cache of the arraylist, because decoding from JSON every time is
        // a waste.
        String key = ViewPagerAdapter.TEAM_TAG;
        Object thing = getCachedObject(key);
        if (thing != null) {
            mData = (ArrayList<TeamMember>) thing;
        } else {
            setCachedObject(key, mData);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Register for updates
        registerForSync(activity, HackTheNorthApplication.Actions.SYNC_TEAM);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the view and return it
        View view = inflater.inflate(R.layout.list_fragment, container, false);

        // Save a reference to the list view
        mListView = (ListView) view.findViewById(android.R.id.list);
        mListView.setFastScrollEnabled(true);

        // Create adapter
        mAdapter = new TeamFragmentAdapter(inflater.getContext(), R.layout.team_list_item, mData);

        // Wrap the adapter in AlphaInAnimatorAdapter for prettiness
        AlphaInAnimationAdapter animationAdapter = new AlphaInAnimationAdapter(mAdapter);
        animationAdapter.setAbsListView(mListView);

        // Hook it up to the ListView
        mListView.setAdapter(animationAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Re-GET data from Firebase here
        if (getActivity() != null) {
            HTTPFirebase.GET("/team", getActivity(),
                    HackTheNorthApplication.Actions.SYNC_TEAM);
        }
    }

    @Override
    protected void handleJSONUpdateInBackground(final String json, String action) {
        final Activity activity = getActivity();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... nothing) {

                // Decode JSON
                final ArrayList<TeamMember> newData = new ArrayList<TeamMember>();
                TeamMember.loadArrayListFromJSON(newData, new TypeToken<HashMap<String, TeamMember>>() {
                        },
                        json);

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
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

            Drawable contact = getContext().getResources().getDrawable(R.drawable.ic_contact);
            contact.setColorFilter(getContext().getResources().getColor(R.color.theme_secondary), PorterDuff.Mode.MULTIPLY);
            contact.setAlpha(!TextUtils.isEmpty(teamMember.email) || !TextUtils.isEmpty(teamMember.twitter) ||
                    !TextUtils.isEmpty(teamMember.phone) ? 222 : 143);

            // Set the data in the TextViews
            ((TextView) convertView.findViewById(R.id.team_member_name)).setText(teamMember.name);
            ((TextView) convertView.findViewById(R.id.team_member_role)).setText(getRolesString(teamMember.role));
            ((ImageView) convertView.findViewById(R.id.team_member_contact)).setImageDrawable(contact);

            convertView.findViewById(R.id.team_member_contact).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO
                }
            });

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
