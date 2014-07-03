package com.hackthenorth.android.view;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hackthenorth.android.R;
import com.hackthenorth.android.framework.HTTPFirebase;
import com.hackthenorth.android.model.Update;

/**
 * A fragment for displaying lists of Update.
 */
public class InfoListFragment extends Fragment {
    public static final String TAG = "InfoListFragment";
    
    // Argument keys
    public static final String DATA_ID = "mDataID";
    
    private String mDataID;
    
    private ListView mListView;
    private ArrayList<Update> mData;
    private InfoListAdapter mAdapter;
    
    /**
     * @param id The data id of this list. Used to determine which data to sync to.
     * @return the InfoListFragment for that data id
     */
    public static InfoListFragment newInstance(String id) {
        InfoListFragment f = new InfoListFragment();
        
        // Add the data ID to the fragment and return it
        Bundle args = new Bundle();
        args.putString(DATA_ID, id);
        f.setArguments(args);
        
        return f;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Extract data from arguments
        Bundle args = getArguments();
        mDataID = args.getString(DATA_ID);
        
        // TODO: Get the list of data from Firebase
        // Use temporary data for now.
        HTTPFirebase.GET(String.format("/%s", mDataID), new HTTPFirebase.Callback() {
            @Override
            public void onSuccess(String json) {
                // Save the list and set up the adapter if we're ready.
                mData = Update.loadUpdateArrayFromJSON(json);
                setupAdapterIfReady();
            }
            
            @Override
            public void onError() {
                // TODO
            }
        });
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
    
    private void setupAdapterIfReady() {
        // Only set up adapter if our data and our listview are ready.
        if (mListView != null && mData != null) {
            
            // Create adapter
            mAdapter = new InfoListAdapter(mListView.getContext(), R.layout.update_list_item_view,
                    mData);
            
            // Hook it up to the listview
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
            
            // Set the data in the view
            ((TextView) convertView.findViewById(R.id.title)).setText(update.title);
            ((TextView) convertView.findViewById(R.id.body)).setText(update.body);
            
            return convertView;
        }
        
        public int getCount() {
            return mData.size();
        }
    }
}
