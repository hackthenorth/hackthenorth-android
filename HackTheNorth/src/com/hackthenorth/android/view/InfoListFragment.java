package com.hackthenorth.android.view;

import java.util.ArrayList;
import java.util.List;

import com.hackthenorth.android.R;
import com.hackthenorth.android.model.Update;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * A fragment for displaying lists of Update. 
 */
public class InfoListFragment extends Fragment {
	
	// Argument keys 
	public static final String DATA_ID = "mDataID";
	
	private String mDataID;
	
	private ListView mList;
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
		mData = Update.loadUpdateArrayFromJSON(
				"[{title:\"Title 1\","
				+ "body: \"Hello. This is an update from Hack The North.\"},"
				+ "{title:\"Title 2\","
				+ "body: \"This is another update from the Hack The North team.\"}]");
		
		setupAdapterIfReady();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		// Inflate the view and return it
		View view = inflater.inflate(R.layout.info_list_fragment, container, false);
		
		// Save a reference to the list view
		mList = (ListView)view.findViewById(android.R.id.list);
		
		// If we're ready, set up the adapter with the list now.
		setupAdapterIfReady();
		
		return view;
	}
	
	private void setupAdapterIfReady() {
		// Only set up adapter if our data and our list are ready.
		if (mList != null && mData != null) {

			// Create adapter
			mAdapter = new InfoListAdapter(mList.getContext(), R.layout.update_list_item_view,
					mData);

			// Hook it up to the listview
			mList.setAdapter(mAdapter);
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
				LayoutInflater inflater = (LayoutInflater)getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(mResource, parent, false);
			}
			
			// Get the data for this position
			Update update = mData.get(position);

			// Set the data in the view
			((TextView)convertView.findViewById(R.id.title)).setText(update.title);
			((TextView)convertView.findViewById(R.id.body)).setText(update.body);

			return convertView;
		}
		
		public int getCount() {
			return mData.size();
		}
	}
}
