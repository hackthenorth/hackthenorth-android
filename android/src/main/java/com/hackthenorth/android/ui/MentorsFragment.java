package com.hackthenorth.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.gson.reflect.TypeToken;
import com.hackthenorth.android.HackTheNorthApplication;
import com.hackthenorth.android.R;
import com.hackthenorth.android.base.BaseListFragment;
import com.hackthenorth.android.framework.HTTPFirebase;
import com.hackthenorth.android.model.Mentor;
import com.hackthenorth.android.ui.dialog.ContactOptionsDialogFragment;
import com.hackthenorth.android.ui.dialog.IntentChooserDialogFragment;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MentorsFragment extends BaseListFragment implements
        ContactOptionsDialogFragment.ListDialogFragmentListener {
    private final String TAG = "MentorsFragment";

    private ArrayList<Mentor> mData = new ArrayList<Mentor>();

    private ListView mListView;
    private ListView mSearchListView;

    private MentorListAdapter mAdapter;
    private MentorListAdapter mSearchAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep a static cache of the arraylist, because decoding from JSON every time is
        // a waste.
        String key = ViewPagerAdapter.MENTORS_TAG;
        Object thing = getCachedObject(key);
        if (thing != null) {
            mData = (ArrayList<Mentor>)thing;
        } else {
            setCachedObject(key, mData);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Register for updates
        registerForSync(activity, HackTheNorthApplication.Actions.SYNC_MENTORS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the view and return it
        View view = inflater.inflate(R.layout.mentors_fragment, container, false);

        // Create adapters
        mAdapter = new MentorListAdapter(inflater.getContext(), R.layout.mentor_list_item, mData,
                this);
        mSearchAdapter = new MentorListAdapter(inflater.getContext(),
                R.layout.mentor_search_list_item, mData, this);

        // Set up list
        mListView = (ListView) view.findViewById(android.R.id.list);
        mListView.setFastScrollEnabled(true);

        mSearchListView = (ListView) view.findViewById(R.id.searchList);

        // Animation adapters
        AlphaInAnimationAdapter animationAdapter = new AlphaInAnimationAdapter(mAdapter);
        animationAdapter.setAbsListView(mListView);
        mListView.setAdapter(animationAdapter);

        AlphaInAnimationAdapter searchAnimationAdapter = new AlphaInAnimationAdapter(mSearchAdapter);
        searchAnimationAdapter.setAbsListView(mSearchListView);
        mSearchListView.setAdapter(searchAnimationAdapter);

        // Hook up activity to fragment so it knows when to dismiss the search box
        if (getActivity() instanceof View.OnTouchListener) {
            View.OnTouchListener l = (View.OnTouchListener)getActivity();
            mSearchListView.setOnTouchListener(l);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Re-GET data from Firebase here
        if (getActivity() != null) {
            HTTPFirebase.GET("/mentors", getActivity(),
                    HackTheNorthApplication.Actions.SYNC_MENTORS);
        }
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
                final ArrayList<Mentor> newData = new ArrayList<Mentor>();
                Mentor.loadArrayListFromJSON(newData, new TypeToken<HashMap<String, Mentor>>(){},
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
                            mSearchAdapter.notifyDataSetChanged();
                        }
                    });
                }

                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public MentorListAdapter getAdapter() {
        return mAdapter;
    }

    public MentorListAdapter getSearchAdapter() {
        return mSearchAdapter;
    }

    @Override
    public void onItemClick(ContactOptionsDialogFragment fragment, int position) {
        // Get the position of the mentor in the list.
        int mentorPosition = fragment.getArguments().getInt(MentorListAdapter.MENTOR_POSITION);
        Mentor mentor = mAdapter.getItem(mentorPosition);

        List<Integer> canonicalContactTypesList = mAdapter.getCanonicalContactTypesList(mentor);
        openContactAction(canonicalContactTypesList.get(position), mentor);

        fragment.dismiss();
    }

    private void openContactAction(int contactType, Mentor mentor) {

        if (getActivity() == null) return;

        Intent intent = null;
        String title = null;
        Resources res = getActivity().getResources();

        switch(contactType) {
            case MentorListAdapter.TWITTER_CONTACT_TYPE:
                String twitter = mentor.twitter;
                twitter = twitter.charAt(0) == '@' ? twitter.substring(1, twitter.length()) : twitter;
                intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(String.format("http://twitter.com/%s", twitter)));
                title = res.getString(R.string.open_twitter_profile);
                break;
            case MentorListAdapter.GITHUB_CONTACT_TYPE:
                String github = mentor.github;
                github = github.charAt(0) == '@' ? github.substring(1, github.length()) : github;
                intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(String.format("http://github.com/%s", github)));
                title = res.getString(R.string.open_github_profile);
                break;
            case MentorListAdapter.EMAIL_CONTACT_TYPE:
                String email = mentor.email;
                intent = new Intent(Intent.ACTION_SENDTO,
                        Uri.fromParts("mailto", email, null));
                title = res.getString(R.string.send_email);
                break;
            case MentorListAdapter.PHONE_CONTACT_TYPE:
                String phone = mentor.phone;
                intent = new Intent(Intent.ACTION_DIAL,
                        Uri.fromParts("tel", phone, null));
                title = res.getString(R.string.dial_phone);
                break;
        }

        IntentChooserDialogFragment dialog = IntentChooserDialogFragment.getInstance(intent,
                getActivity(), title);
        dialog.show(getFragmentManager(), "another hmmmm?");
    }

    @Override
    public void onCancelButtonClick(ContactOptionsDialogFragment fragment) {
        fragment.dismiss();
    }
}
