package com.hackthenorth.android.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

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
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.gson.reflect.TypeToken;
import com.hackthenorth.android.HackTheNorthApplication;
import com.hackthenorth.android.R;
import com.hackthenorth.android.base.BaseListFragment;
import com.hackthenorth.android.framework.HTNNotificationManager;
import com.hackthenorth.android.framework.HTTPFirebase;
import com.hackthenorth.android.framework.NetworkManager;
import com.hackthenorth.android.model.Update;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;

/**
 * A fragment for displaying lists of Update.
 */
public class UpdatesFragment extends BaseListFragment {
    public static final String TAG = "UpdatesFragment";

    private ListView mListView;
    private ArrayList<Update> mData = new ArrayList<Update>();
    private UpdatesFragmentAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep a static cache of the arraylist, because decoding from JSON every time is
        // a waste.
        String key = ViewPagerAdapter.UPDATES_TAG;
        Object thing = getCachedObject(key);
        if (thing != null) {
            mData = (ArrayList<Update>)thing;
        } else {
            setCachedObject(key, mData);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Register for updates
        registerForSync(activity, HackTheNorthApplication.Actions.SYNC_UPDATES);

        HTTPFirebase.GET("/updates", activity, HackTheNorthApplication.Actions.SYNC_UPDATES);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the view and return it
        View view = inflater.inflate(R.layout.list_fragment, container, false);

        // Create adapter
        mAdapter = new UpdatesFragmentAdapter(inflater.getContext(), R.layout.update_list_item,
                mData);

        // Set up list
        mListView = (ListView) view.findViewById(android.R.id.list);

        // Animation adapter
        AlphaInAnimationAdapter animationAdapter = new AlphaInAnimationAdapter(mAdapter);
        animationAdapter.setAbsListView(mListView);
        mListView.setAdapter(animationAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Clear the notifications
        HTNNotificationManager.clearUpdatesNotification(getActivity());
    }

    @Override
    protected void handleJSONUpdateInBackground(final String json, String action) {
        final Activity activity = getActivity();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... nothing) {

                final ArrayList<Update> newData = new ArrayList<Update>();
                Update.loadArrayListFromJSON(newData, new TypeToken<HashMap<String, Update>>(){},
                        json);

                if (activity != null && mAdapter != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Copy the data into the ListView on the main thread and
                            // refresh.

                            // Note that both of the data lists are sorted, with the
                            // newest items first. We want to avoid adding a bunch of
                            // elements to the front of the ArrayList, which each takes
                            // O(n) time...

                            // If there's new data, then copy over all the elements in
                            // newData to mData, and refresh the ListView.
                            if (newData.size() > mData.size()) {
                                int delta = newData.size() - mData.size();

                                // Append that many elements to the end of mData
                                // (so mData.size() == newData.size())
                                for (int i = 0; i < delta; i++) {
                                    mData.add(null);
                                }

                                // Now, copy all the elements from newData into mData.
                                for (int i = 0; i < newData.size(); i++) {
                                    mData.set(i, newData.get(i));
                                }

                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }

                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static class UpdatesFragmentAdapter extends ArrayAdapter<Update> {
        private int mResource;
        private ArrayList<Update> mData;
        
        public UpdatesFragmentAdapter(Context context, int resource, ArrayList<Update> objects) {
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
            Update update = mData.get(position);

            // Set up the image view with the avatar URLs
            NetworkImageView networkImageView = (NetworkImageView)
                    convertView.findViewById(R.id.update_avatar);
            networkImageView.setDefaultImageResId(R.drawable.ic_launcher);

            // If we have an avatar URL, load it here.
            ImageLoader loader = NetworkManager.getImageLoader();
            if (!"".equals(update.avatar)) {
                networkImageView.setImageUrl(update.avatar, loader);
            } else {
                networkImageView.setImageUrl(null, loader);
            }

            // Set the data in the TextViews
            ((TextView) convertView.findViewById(R.id.update_name))
                    .setText(update.name);
            if (update.time != null) {
                ((TextView) convertView.findViewById(R.id.update_date))
                        .setText(getRelativeTimestamp(update.time));
            }
            ((TextView) convertView.findViewById(R.id.update_description))
                    .setText(update.description);

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
    }
}
