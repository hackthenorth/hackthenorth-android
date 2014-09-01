package com.hackthenorth.android.ui;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.hackthenorth.android.HackTheNorthApplication;
import com.hackthenorth.android.R;
import com.hackthenorth.android.base.BaseListFragment;
import com.hackthenorth.android.framework.HTTPFirebase;
import com.hackthenorth.android.model.Mentor;

import java.util.ArrayList;

public class MentorsFragment extends BaseListFragment {
    private final String TAG = "MentorsFragment";

    private ArrayList<Mentor> mData = new ArrayList<Mentor>();

    private ListView mListView;
    private ListView mSearchListView;
    private MentorListAdapter mAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Keep a static cache of the arraylist, because decoding from JSON every time is
        // a waste.
        String key = ViewPagerAdapter.MENTORS_TAG;
        Object thing = getCachedObject(key);
        if (thing != null) {
            mData = (ArrayList<Mentor>)thing;
        } else {
            setCachedObject(key, mData);
        }

        // Create adapters
        mAdapter = new MentorListAdapter(activity, R.layout.mentor_list_item, mData);

        // Register for updates
        registerForSync(activity, HackTheNorthApplication.Actions.SYNC_MENTORS);

        HTTPFirebase.GET("/mentors", activity, HackTheNorthApplication.Actions.SYNC_MENTORS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the view and return it
        View view = inflater.inflate(R.layout.mentors_fragment, container, false);

        // Set up list
        mListView = (ListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);
        mListView.setFastScrollEnabled(true);

        mSearchListView = (ListView) view.findViewById(R.id.searchList);
        mSearchListView.setAdapter(mAdapter);

        // Hook up activity to fragment so it knows when to dismiss the search box
        if (getActivity() instanceof View.OnTouchListener) {
            View.OnTouchListener l = (View.OnTouchListener)getActivity();
            mSearchListView.setOnTouchListener(l);
        }

        return view;
    }

    public void startSearch() {
        mListView.setVisibility(View.GONE);
        mSearchListView.setVisibility(View.VISIBLE);

        mAdapter.setResource(R.layout.mentor_search_list_item);
        mAdapter.notifyDataSetChanged();
    }

    public void endSearch() {
        mListView.setVisibility(View.VISIBLE);
        mSearchListView.setVisibility(View.GONE);

        mAdapter.setResource(R.layout.mentor_list_item);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void handleJSONUpdateInBackground(final String json, String action) {
        final Activity activity = getActivity();
        new AsyncTask<Void, Void, Void>() {
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

    public MentorListAdapter getAdapter() {
        return mAdapter;
    }
}
