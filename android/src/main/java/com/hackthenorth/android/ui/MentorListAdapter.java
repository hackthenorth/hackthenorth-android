package com.hackthenorth.android.ui;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;

import com.hackthenorth.android.R;
import com.hackthenorth.android.framework.FuzzySearchIndexer;
import com.hackthenorth.android.model.Mentor;
import com.hackthenorth.android.ui.component.TextView;
import com.hackthenorth.android.ui.dialog.ContactOptionsDialogFragment;
import com.hackthenorth.android.util.DateFormatter;

import java.util.ArrayList;
import java.util.List;

public class MentorListAdapter extends ArrayAdapter<Mentor> implements SectionIndexer {

    private final String TAG = "MentorListAdapter";

    private final String sections = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    // Info for the fragment
    public static final String MENTOR_POSITION = "mentorPosition";

    // Contact types
    public static final int EMAIL_CONTACT_TYPE = 0;
    public static final int PHONE_CONTACT_TYPE = 1;
    public static final int GITHUB_CONTACT_TYPE = 2;
    public static final int TWITTER_CONTACT_TYPE = 3;

    private Context mContext;
    private int mResource;
    private ArrayList<Mentor> mData;
    private FuzzySearchIndexer<Mentor> mIndexer;
    private MentorsFragment mFragment;

    public MentorListAdapter(Context context, int resource, ArrayList<Mentor> objects,
                             MentorsFragment fragment) {
        super(context, resource, objects);

        mContext = context;
        mResource = resource;
        mData = objects;
        mIndexer = new FuzzySearchIndexer<Mentor>(mData);
        mFragment = fragment;
    }

    public void setResource(int resource) {
        mResource = resource;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null || convertView.getId() != mResource) {
            // If we don't have a view to reuse, inflate a new one.
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mResource, parent, false);
        }

        // Get the data for this position
        final Mentor mentor = mData.get(position);

        if (contactable(mentor)) {
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Show a list dialog fragment for contacting the mentors.
                    ArrayList<String> titles = new ArrayList<String>();
                    ArrayList<String> items = new ArrayList<String>();

                    if (!TextUtils.isEmpty(mentor.email)) {
                        titles.add(mContext.getString(R.string.email_mentor));
                        items.add(mentor.email);
                    }
                    if (!TextUtils.isEmpty(mentor.twitter)) {
                        titles.add(mContext.getString(R.string.twitter_mentor));
                        items.add(mentor.twitter);
                    }
                    if (!TextUtils.isEmpty(mentor.github)) {
                        titles.add(mContext.getString(R.string.github_mentor));
                        items.add(mentor.github);
                    }
                    if (!TextUtils.isEmpty(mentor.phone)) {
                        titles.add(mContext.getString(R.string.phone_mentor));
                        items.add(mentor.phone);
                    }

                    Resources res = mContext.getResources();
                    ContactOptionsDialogFragment dialog = ContactOptionsDialogFragment.getInstance(mFragment,
                            res.getString(R.string.contact_mentor),
                            null, titles, items,
                            res.getString(R.string.dialog_button_cancel));

                    Bundle args = dialog.getArguments();
                    args.putInt(MENTOR_POSITION, position);
                    dialog.setArguments(args);

                    dialog.show(mFragment.getFragmentManager(), "heythere");
                }
            });
        }

        // Set the data in the TextViews
        if (convertView.findViewById(R.id.mentor_name) != null) {
            ((TextView) convertView.findViewById(R.id.mentor_name)).setText(mentor.name);
        }
        if (convertView.findViewById(R.id.mentor_organization) != null) {
            ((TextView) convertView.findViewById(R.id.mentor_organization)).setText(mentor.organization);
        }
        if (convertView.findViewById(R.id.mentor_availability) != null) {
            ((TextView) convertView.findViewById(R.id.mentor_availability)).setText(getAvailabilityString(mentor.availability));
        }
        if (convertView.findViewById(R.id.mentor_skills) != null) {
            ((TextView) convertView.findViewById(R.id.mentor_skills)).setText(getSkillsString(mentor.skills));
        }

        // Show methods of contacting them
        if (convertView.findViewById(R.id.mentor_email) != null && !TextUtils.isEmpty(mentor.email)) {
            convertView.findViewById(R.id.mentor_email).setVisibility(View.VISIBLE);
        }
        if (convertView.findViewById(R.id.mentor_twitter) != null && !TextUtils.isEmpty(mentor.twitter)) {
            convertView.findViewById(R.id.mentor_twitter).setVisibility(View.VISIBLE);
        }
        if (convertView.findViewById(R.id.mentor_github) != null && !TextUtils.isEmpty(mentor.github)) {
            convertView.findViewById(R.id.mentor_github).setVisibility(View.VISIBLE);
        }
        if (convertView.findViewById(R.id.mentor_phone) != null && !TextUtils.isEmpty(mentor.phone)) {
            convertView.findViewById(R.id.mentor_phone).setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    public int getCount() {
        return mData.size();
    }

    public boolean contactable(Mentor mentor) {
        return mentor.github != null ||
                mentor.twitter != null ||
                mentor.phone != null ||
                mentor.email != null;
    }

    /**
     * @param mentor The mentor we wish to contact.
     * @return A list of the ways that a mentor can be contacted in a canonical order.
     */
    public List<Integer> getCanonicalContactTypesList(Mentor mentor) {
        List<Integer> list = new ArrayList<Integer>();
        if (mentor.email != null) list.add(EMAIL_CONTACT_TYPE);
        if (mentor.twitter != null) list.add(TWITTER_CONTACT_TYPE);
        if (mentor.github != null) list.add(GITHUB_CONTACT_TYPE);
        if (mentor.phone != null) list.add(PHONE_CONTACT_TYPE);
        return list;
    }

    private String getAvailabilityString(ArrayList<ArrayList<String>> timeslots) {

        if (timeslots == null || timeslots.size() == 0) {
            return null;
        }

        String availability = "";

        for (int i = 0; i < timeslots.size(); i++) {
            availability += DateFormatter.getTimespanString(timeslots.get(i));

            if (i != timeslots.size() - 1) {
                availability += "\n";
            }
        }

        return availability;
    }

    private String getSkillsString(ArrayList<String> skillsList) {

        if (skillsList == null || skillsList.size() == 0) {
            return null;
        }

        String skills = skillsList.get(0);

        for (int i = 1; i < skillsList.size(); i++) {
            skills += " • " + skillsList.get(i);
        }

        return skills;
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

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        mIndexer.updateData(mData);
    }

    public void query(String queryString) {
        mIndexer.query(queryString);
        super.notifyDataSetChanged();
    }
}
