package com.hackthenorth.android.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.hackthenorth.android.model.Mentor;
import com.hackthenorth.android.model.TeamMember;
import com.hackthenorth.android.ui.dialog.ContactOptionsDialogFragment;
import com.hackthenorth.android.ui.dialog.IntentChooserDialogFragment;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TeamFragment extends BaseListFragment implements ContactOptionsDialogFragment.ListDialogFragmentListener {
    public static final String TAG = "TeamFragment";

    // Info for the fragment
    public static final String TEAMMEMBER_POSITION = "mentorPosition";

    // Contact types
    public static final int EMAIL_CONTACT_TYPE = 0;
    public static final int PHONE_CONTACT_TYPE = 1;
    public static final int GITHUB_CONTACT_TYPE = 2;
    public static final int TWITTER_CONTACT_TYPE = 3;

    private ListView mListView;
    private ArrayList<TeamMember> mData = new ArrayList<TeamMember>();
    private TeamFragmentAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addHardcodedTeamData(mData);

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
        View view = inflater.inflate(R.layout.team_fragment, container, false);

        // Save a reference to the list view
        mListView = (ListView) view.findViewById(android.R.id.list);
        mListView.setFastScrollEnabled(true);

        // Create adapter
        mAdapter = new TeamFragmentAdapter(inflater.getContext(), this, R.layout.team_list_item, mData);

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
                            mData.clear();

                            // Add Kartik and Kevin's data hardcoded here
                            addHardcodedTeamData(mData);

                            mData.addAll(newData);
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                }

                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onItemClick(ContactOptionsDialogFragment fragment, int position) {
        // Get the position of the mentor in the list.
        int mentorPosition = fragment.getArguments().getInt(MentorListAdapter.MENTOR_POSITION);
        TeamMember mentor = mAdapter.getItem(mentorPosition);

        List<Integer> canonicalContactTypesList = getCanonicalContactTypesList(mentor);
        openContactAction(canonicalContactTypesList.get(position), mentor);

        fragment.dismiss();
    }

    @Override
    public void onCancelButtonClick(ContactOptionsDialogFragment fragment) {
        fragment.dismiss();
    }

    /**
     * @param mentor The mentor we wish to contact.
     * @return A list of the ways that a mentor can be contacted in a canonical order.
     */
    public List<Integer> getCanonicalContactTypesList(TeamMember mentor) {
        List<Integer> list = new ArrayList<Integer>();
        if (mentor.email != null) list.add(EMAIL_CONTACT_TYPE);
        if (mentor.twitter != null) list.add(TWITTER_CONTACT_TYPE);
        if (mentor.phone != null) list.add(PHONE_CONTACT_TYPE);
        return list;
    }

    public void openContactAction(int contactType, TeamMember teamMember) {

        if (getActivity() == null) return;

        Intent intent = null;
        String title = null;
        Resources res = getActivity().getResources();

        switch (contactType) {
            case TeamFragmentAdapter.TWITTER_CONTACT_TYPE:
                String twitter = teamMember.twitter;
                twitter = twitter.charAt(0) == '@' ? twitter.substring(1, twitter.length()) : twitter;
                intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(String.format("http://twitter.com/%s", twitter)));
                title = res.getString(R.string.open_twitter_profile);
                break;
            case TeamFragmentAdapter.EMAIL_CONTACT_TYPE:
                String email = teamMember.email;
                intent = new Intent(Intent.ACTION_SENDTO,
                        Uri.fromParts("mailto", email, null));
                title = res.getString(R.string.send_email);
                break;
            case TeamFragmentAdapter.PHONE_CONTACT_TYPE:
                String phone = teamMember.phone;
                intent = new Intent(Intent.ACTION_DIAL,
                        Uri.fromParts("tel", phone, null));
                title = res.getString(R.string.dial_phone);
                break;
        }

        IntentChooserDialogFragment dialog = IntentChooserDialogFragment.getInstance(intent,
                getActivity(), title);
        dialog.show(getFragmentManager(), "another hmmmm?");
    }

    private void addHardcodedTeamData(ArrayList<TeamMember> mData) {

        TeamMember kartik = new TeamMember();
        kartik.name = "Kartik Talwar";
        kartik.phone = "+16472254089";
        kartik.email = "kartik@hackthenorth.com";
        kartik.role = new ArrayList<String>(Arrays.asList("organizer"));

        TeamMember kevin = new TeamMember();
        kevin.name = "Kevin Lau";
        kevin.phone = "+16476278630";
        kevin.email = "kevin@hackthenorth.com";
        kevin.role = new ArrayList<String>(Arrays.asList("organizer"));

        mData.add(kartik);
        mData.add(kevin);
    }

    public static class TeamFragmentAdapter extends ArrayAdapter<TeamMember> {

        // Contact types
        public static final int EMAIL_CONTACT_TYPE = 0;
        public static final int PHONE_CONTACT_TYPE = 1;
        public static final int GITHUB_CONTACT_TYPE = 2;
        public static final int TWITTER_CONTACT_TYPE = 3;

        private Context mContext;
        private TeamFragment mFragment;
        private int mResource;
        private ArrayList<TeamMember> mData;

        public TeamFragmentAdapter(Context context, TeamFragment fragment, int resource, ArrayList<TeamMember> objects) {
            super(context, resource, objects);
            mContext = context;
            mFragment = fragment;
            mResource = resource;
            mData = objects;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                // If we don't have a view to reuse, inflate a new one.
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(mResource, parent, false);
            }

            // Get the data for this position
            final TeamMember teamMember = mData.get(position);

            /* DISABLED AVATARS
            // Set up the image view with the avatar URLs
            NetworkImageView networkImageView = (NetworkImageView) convertView.findViewById(R.id.team_member_avatar);
            networkImageView.setDefaultImageResId(R.drawable.ic_launcher);

            // If we have an avatar URL, load it here.
            ImageLoader loader = NetworkManager.getImageLoader();
            if (!"" .equals(teamMember.avatar)) {
                networkImageView.setImageUrl(teamMember.avatar, loader);
            } else {
                networkImageView.setImageUrl(null, loader);
            }
            */

            Drawable contact = getContext().getResources().getDrawable(R.drawable.ic_contact);
            contact.setColorFilter(getContext().getResources().getColor(R.color.blue_dark), PorterDuff.Mode.MULTIPLY);
            contact.setAlpha(contactable(teamMember) ? 222 : 143);

            // Set the data in the TextViews
            ((TextView) convertView.findViewById(R.id.team_member_name)).setText(teamMember.name);
            ((TextView) convertView.findViewById(R.id.team_member_role)).setText(getRolesString(teamMember.role));
            ((ImageView) convertView.findViewById(R.id.team_member_contact)).setImageDrawable(contact);

            if (contactable(teamMember)) {
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Show a list dialog fragment for contacting the teamMembers.
                        ArrayList<String> titles = new ArrayList<String>();
                        ArrayList<String> items = new ArrayList<String>();

                        if (!TextUtils.isEmpty(teamMember.email)) {
                            titles.add(mContext.getString(R.string.email_mentor));
                            items.add(teamMember.email);
                        }
                        if (!TextUtils.isEmpty(teamMember.twitter)) {
                            titles.add(mContext.getString(R.string.twitter_mentor));
                            items.add(teamMember.twitter);
                        }
                        if (!TextUtils.isEmpty(teamMember.phone)) {
                            titles.add(mContext.getString(R.string.phone_mentor));
                            items.add(teamMember.phone);
                        }

                        Resources res = mContext.getResources();
                        ContactOptionsDialogFragment dialog = ContactOptionsDialogFragment.getInstance(mFragment,
                                res.getString(R.string.contact_mentor),
                                null, titles, items,
                                res.getString(R.string.dialog_button_cancel));

                        Bundle args = dialog.getArguments();
                        args.putInt(TEAMMEMBER_POSITION, position);
                        dialog.setArguments(args);

                        dialog.show(mFragment.getFragmentManager(), "heythere");
                    }
                });
            }

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

        public boolean contactable(TeamMember team) {
            return team.twitter != null ||
                    team.phone != null ||
                    team.email != null;
        }
    }
}
