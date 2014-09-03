package com.hackthenorth.android.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.hackthenorth.android.HackTheNorthApplication;
import com.hackthenorth.android.R;
import com.hackthenorth.android.base.BaseListFragment;
import com.hackthenorth.android.framework.HTTPFirebase;
import com.hackthenorth.android.model.Instruction;
import com.hackthenorth.android.model.Model;
import com.hackthenorth.android.model.ScheduleItem;
import com.hackthenorth.android.ui.dialog.ConfirmDialogFragment;
import com.hackthenorth.android.util.DateFormatter;
import com.hackthenorth.android.ui.dialog.ConfirmDialogFragment;
import com.hackthenorth.android.ui.dialog.ConfirmDialogFragment.ConfirmDialogFragmentListener;

/**
 * A fragment for displaying lists of Update.
 */
public class ScheduleFragment extends BaseListFragment
        implements ConfirmDialogFragmentListener {

    public static final String TAG = "ScheduleFragment";

    public static final String CONFIRM_DIALOG_TAG = "ConfirmDialog";
    public static final String CONFIRM_DIALOG_POSITION_KEY = "position";

    private ListView mListView;
    private ScheduleFragmentAdapter mAdapter;
    private ArrayList<Model> mData = new ArrayList<Model>();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Keep a static cache of the arraylist, because decoding from JSON every time is
        // a waste.
        String key = ViewPagerAdapter.SCHEDULE_TAG;
        Object thing = getCachedObject(key);
        if (thing != null) {
            mData = (ArrayList<Model>)thing;
        } else {
            setCachedObject(key, mData);
        }

        // Set up adapter
        mAdapter = new ScheduleFragmentAdapter(activity, R.layout.schedule_list_item, mData);
        mAdapter.setFragment(this);

        // Register for updates
        registerForSync(activity, HackTheNorthApplication.Actions.SYNC_SCHEDULE);

        HTTPFirebase.GET("/schedule", activity,
                HackTheNorthApplication.Actions.SYNC_SCHEDULE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the view and return it
        View view = inflater.inflate(R.layout.list_fragment_cards, container, false);

        // Save a reference to the list view
        mListView = (ListView) view.findViewById(android.R.id.list);

        // Hook it up to the ListView
        mListView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Re-GET data from Firebase here
        if (getActivity() != null) {
            HTTPFirebase.GET("/schedule", getActivity(),
                    HackTheNorthApplication.Actions.SYNC_SCHEDULE);
        }
    }

    @Override
    public void onPositiveClick(ConfirmDialogFragment fragment) {
        // Get the email intent from the ScheduleFragmentAdapter and start it.
        int position = fragment.getArguments().getInt(CONFIRM_DIALOG_POSITION_KEY);
        startActivity(mAdapter.getIntent(position));
    }

    @Override
    public void onNegativeClick(ConfirmDialogFragment fragment) {
        // Do nothing
    }

    @Override
    protected void handleJSONUpdateInBackground(final String json, String actio) {
        final Activity activity = getActivity();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... nothing) {

                // Decode JSON
                final ArrayList<ScheduleItem> newData = new ArrayList<ScheduleItem>();
                ScheduleItem.loadArrayListFromJSON(newData, new TypeToken<HashMap<String, ScheduleItem>>(){},
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
        }.execute();
    }

    public static class ScheduleFragmentAdapter extends ArrayAdapter<Model> {
        private int mResource;
        private ArrayList<Model> mData;
        private Fragment mFragment;

        public ScheduleFragmentAdapter(Context context, int resource,
                                       ArrayList<Model> objects) {
            super(context, resource, objects);

            mResource = resource;
            mData = objects;
        }

        public void setFragment(Fragment fragment) {
            mFragment = fragment;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {

            // Get the data for this position
            Model model = mData.get(position);

            if (model instanceof ScheduleItem) {
                final ScheduleItem scheduleItem = (ScheduleItem) model;

                if (convertView == null ||
                        convertView.getId() != R.id.schedule_list_item) {
                    // If we don't have a view to reuse, inflate a new one.
                    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                            Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(mResource, parent, false);
                }

                // Set up the click event
                final Intent intent = getIntent(position);
                final Context context = convertView.getContext();

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Resources res = context.getResources();
                        String title = res.getString(R.string.calendar_dialog_title);
                        String message = String.format(res.getString(R.string.calendar_dialog_message),
                                scheduleItem.name);
                        String yes = res.getString(R.string.dialog_button_yes);
                        String cancel = res.getString(R.string.dialog_button_cancel);

                        ConfirmDialogFragment dialog = ConfirmDialogFragment.getInstance(
                                title, message, yes, cancel);

                        // Set the target fragment for the callback
                        dialog.setTargetFragment(mFragment, 0);

                        // Keep track of the position here so the fragment knows which
                        // intent to send off.
                        Bundle args = dialog.getArguments();
                        args.putInt(CONFIRM_DIALOG_POSITION_KEY, position);
                        dialog.setArguments(args);

                        // Add the dialog to the fragment.
                        dialog.show(mFragment.getFragmentManager(), CONFIRM_DIALOG_TAG);
                    }
                });

                ArrayList<String> times = new ArrayList<String>();
                times.add(scheduleItem.start_time);
                times.add(scheduleItem.end_time);

                convertView.findViewById(R.id.schedule_item_type)
                        .setBackgroundDrawable(getIndicator(scheduleItem.type));
                ((TextView) convertView.findViewById(R.id.schedule_item_name))
                        .setText(scheduleItem.name);
                ((TextView) convertView.findViewById(R.id.schedule_item_description))
                        .setText(scheduleItem.description);
                ((TextView) convertView.findViewById(R.id.schedule_item_speaker))
                        .setText(scheduleItem.speaker);
                ((TextView) convertView.findViewById(R.id.schedule_item_time))
                        .setText(DateFormatter.getTimespanString(times));

            } else if (model instanceof Instruction) {
                final Instruction instruction = (Instruction) model;

                if (convertView == null ||
                        convertView.getId() != R.id.instruction_list_item) {

                    // If we don't have a view to reuse, inflate a new one.
                    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                            Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.instruction_list_item, parent, false);
                }
            }

            return convertView;
        }

        public Intent getIntent(int position) {
            Model model = mData.get(position);
            if (!(model instanceof ScheduleItem)) {
                return null;
            }
            ScheduleItem item = (ScheduleItem) model;

            SimpleDateFormat format = DateFormatter.getISO8601SimpleDateFormat();
            Date start = null, end = null;
            try {
                start = format.parse(item.start_time);
                end = format.parse(item.end_time);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Intent result = null;
            if (start != null && end != null) {
                result = new Intent(Intent.ACTION_INSERT)
                        .setType("vnd.android.cursor.item/event")
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start.getTime())
                        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end.getTime())
                        .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
                        .putExtra(Events.TITLE, item.name)
                        .putExtra(Events.DESCRIPTION, item.description)
                        .putExtra(Events.EVENT_LOCATION, item.location)
                        .putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY)
                        .putExtra(Events.ACCESS_LEVEL, Events.ACCESS_PRIVATE);
            }

            return result;
        }

        public Drawable getIndicator(String type) {
            Resources res = getContext().getResources();
            Drawable indicator = res.getDrawable(R.drawable.schedule_item_type);

            if (type.equals(ScheduleItem.TYPE_EVENT)) {
                indicator.setColorFilter(res.getColor(R.color.theme_secondary), PorterDuff.Mode.MULTIPLY);
            } else if (type.equals(ScheduleItem.TYPE_WORKSHOP)) {
                indicator.setColorFilter(res.getColor(R.color.workshop), PorterDuff.Mode.MULTIPLY);
            } else if (type.equals(ScheduleItem.TYPE_TALK)) {
                indicator.setColorFilter(res.getColor(R.color.talk), PorterDuff.Mode.MULTIPLY);
            } else if (type.equals(ScheduleItem.TYPE_SPEAKER)) {
                indicator.setColorFilter(res.getColor(R.color.speaker), PorterDuff.Mode.MULTIPLY);
            } else if (type.equals(ScheduleItem.TYPE_UPDATE)) {
                indicator.setColorFilter(res.getColor(R.color.update), PorterDuff.Mode.MULTIPLY);
            } else if (type.equals(ScheduleItem.TYPE_FOOD)) {
                indicator.setColorFilter(res.getColor(R.color.food), PorterDuff.Mode.MULTIPLY);
            }

            return indicator;
        }

        public int getCount() {
            return mData.size();
        }
    }
}
