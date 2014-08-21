package com.hackthenorth.android.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hackthenorth.android.HackTheNorthApplication;
import com.hackthenorth.android.R;
import com.hackthenorth.android.base.BaseListFragment;
import com.hackthenorth.android.framework.HTTPFirebase;
import com.hackthenorth.android.model.Instruction;
import com.hackthenorth.android.model.Model;
import com.hackthenorth.android.model.ScheduleItem;
import com.hackthenorth.android.util.DateTimeUtil;
import com.hackthenorth.android.ui.ConfirmDialogFragment.ConfirmDialogFragmentListener;

/**
 * A fragment for displaying lists of Update.
 */
public class ScheduleFragment extends BaseListFragment
        implements ConfirmDialogFragmentListener {

    public static final String TAG = "UpdateListFragment";

    public static final String CONFIRM_DIALOG_TAG = "ConfirmDialog";
    public static final String CONFIRM_DIALOG_POSITION_KEY = "position";

    private ListView mListView;
    private ScheduleFragmentAdapter mAdapter;
    private ArrayList<Model> mData = new ArrayList<Model>();
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Set up BroadcastReceiver for updates.
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (HackTheNorthApplication.Actions.SYNC_SCHEDULE
                        .equals(intent.getAction())) {

                    // Update with the new data
                    String key = intent.getAction();
                    String json = intent.getStringExtra(key);
                    handleJSONInBackground(json, mAdapter);
                }
            }
        };

        // Register our broadcast receiver.
        IntentFilter filter = new IntentFilter();
        filter.addAction(HackTheNorthApplication.Actions.SYNC_SCHEDULE);

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(activity);
        manager.registerReceiver(mBroadcastReceiver, filter);

        HTTPFirebase.GET("/schedule", activity,
                HackTheNorthApplication.Actions.SYNC_SCHEDULE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the view and return it
        View view = inflater.inflate(R.layout.schedule_fragment, container, false);

        // Save a reference to the list view
        mListView = (ListView) view.findViewById(android.R.id.list);

        // Set up adapter
        mAdapter = new ScheduleFragmentAdapter(mListView.getContext(),
                R.layout.schedule_list_item, mData);
        mAdapter.setFragment(this);

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
    protected void onUpdate(String json) {

        ArrayList<ScheduleItem> newData = ScheduleItem.loadScheduleFromJSON(json);

        mData.clear();
        mData.addAll(newData);
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
                final ScheduleItem scheduleItem = (ScheduleItem)model;

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

                ((TextView)convertView.findViewById(R.id.schedule_item_name))
                        .setText(scheduleItem.name);
                ((TextView)convertView.findViewById(R.id.schedule_item_description))
                        .setText(scheduleItem.description);
                ((TextView)convertView.findViewById(R.id.schedule_item_speaker))
                        .setText(scheduleItem.speaker);

                // TODO: This probably shouldn't be text; use the string to display an icon,
                // TODO: or style the card accordingly.
                ((TextView)convertView.findViewById(R.id.schedule_item_type))
                        .setText(scheduleItem.type);
                ((TextView)convertView.findViewById(R.id.schedule_item_start_time))
                        .setText(scheduleItem.start_time);
                ((TextView)convertView.findViewById(R.id.schedule_item_end_time))
                        .setText(scheduleItem.end_time);

            } else if (model instanceof Instruction) {
                final Instruction instruction = (Instruction)model;

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
            ScheduleItem item = (ScheduleItem)model;

            SimpleDateFormat format = DateTimeUtil.getISO8601SimpleDateFormat();
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
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                start.getTime())
                        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                                end.getTime())
                        .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
                        .putExtra(Events.TITLE, item.name)
                        .putExtra(Events.DESCRIPTION, item.description)
                        .putExtra(Events.EVENT_LOCATION, item.location)
                        .putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY)
                        .putExtra(Events.ACCESS_LEVEL, Events.ACCESS_PRIVATE);
            }

            return result;
        }

        public int getCount() {
            return mData.size();
        }
    }
}
