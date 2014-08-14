package com.hackthenorth.android.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.hackthenorth.android.HackTheNorthApplication;
import com.hackthenorth.android.R;
import com.hackthenorth.android.framework.HTNNotificationManager;
import com.hackthenorth.android.framework.HTTPFirebase;
import com.hackthenorth.android.framework.NetworkManager;
import com.hackthenorth.android.model.Update;

/**
 * A fragment for displaying lists of Update.
 */
public class UpdateListFragment extends Fragment {
    public static final String TAG = "UpdateListFragment";

    // Argument keys
    public static final String DATA_ID = "mDataID";

    private ListView mListView;
    private ArrayList<Update> mData;
    private InfoListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Extract data from arguments
        Bundle args = getArguments();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        HTTPFirebase.GET("/updates", activity,
                HackTheNorthApplication.Actions.SYNC_UPDATES);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the view and return it
        View view = inflater.inflate(R.layout.info_list_fragment, container, false);
        
        // Save a reference to the list view
        mListView = (ListView) view.findViewById(android.R.id.list);
        
        // If we're ready, set up the adapter with the list now.
        setupAdapterIfReady();
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Clear the notifications
        HTNNotificationManager.clearUpdatesNotification(getActivity());
    }

    // Receive a JSON update
    public void onUpdate(String json) {

        // Set or update our data
        if (mData == null) {
            mData = Update.loadUpdateArrayFromJSON(json);

            // If we're ready, set up the adapter with the list.
            setupAdapterIfReady();

        } else {
            // TODO: Is JSON parsing fast enough to be on the main thread?
            ArrayList<Update> newData = Update.loadUpdateArrayFromJSON(json);

            // Note that both of the data lists are sorted, with the newest items first.
            // We want to avoid adding a bunch of elements to the front of the ArrayList,
            // which each takes O(n) time...

            // If there's new data, then copy over all the elements in newData to mData,
            // and refresh the ListView.
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
    }
    
    private void setupAdapterIfReady() {
        // Only set up adapter if our ListView and our data are ready.
        if (mListView != null && mData != null) {
            
            // Create adapter
            mAdapter = new InfoListAdapter(mListView.getContext(), R.layout.update_list_item_view,
                    mData);
            
            // Hook it up to the ListView
            mListView.setAdapter(mAdapter);
        }
    }
    
    public static class InfoListAdapter extends ArrayAdapter<Update> {
        private int mResource;
        private ArrayList<Update> mData;
        
        public InfoListAdapter(Context context, int resource, ArrayList<Update> objects) {
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
            ((TextView) convertView.findViewById(R.id.update_date))
                    .setText(getRelativeTimestamp(update.time));
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
