package com.hackthenorth.android.ui;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.hackthenorth.android.HackTheNorthApplication;
import com.hackthenorth.android.R;
import com.hackthenorth.android.base.BaseListFragment;
import com.hackthenorth.android.framework.HTTPFirebase;
import com.hackthenorth.android.model.Prize;
import com.hackthenorth.android.ui.component.TextView;
import com.hackthenorth.android.ui.dialog.ConfirmDialogFragment;
import com.hackthenorth.android.ui.dialog.ConfirmDialogFragment.ConfirmDialogFragmentListener;
import com.hackthenorth.android.ui.dialog.IntentChooserDialogFragment;

import java.util.ArrayList;

public class PrizesFragment extends BaseListFragment implements
        ConfirmDialogFragmentListener {

    public static final String TAG = "UpdateListFragment";

    public static final String CONFIRM_DIALOG_TAG = "ConfirmDialog";
    public static final String CONFIRM_DIALOG_POSITION_KEY = "position";
    private static final String INTENT_CHOOSER_DIALOG_FRAGMENT = "intentChooserDialogFragment";

    private ListView mListView;
    private ArrayList<Prize> mData = new ArrayList<Prize>();
    private PrizesFragmentAdapter mAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mAdapter = new PrizesFragmentAdapter(activity, R.layout.prizes_list_item, mData);
        mAdapter.setFragment(this);

        registerForSync(activity, HackTheNorthApplication.Actions.SYNC_PRIZES, mAdapter);

        HTTPFirebase.GET("/prizes", activity,
                HackTheNorthApplication.Actions.SYNC_PRIZES);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the view and return it
        View view = inflater.inflate(R.layout.prizes_fragment, container, false);

        // Set up list
        mListView = (ListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Re-GET data from Firebase here
        if (getActivity() != null) {
            HTTPFirebase.GET("/prizes", getActivity(),
                    HackTheNorthApplication.Actions.SYNC_PRIZES);
        }
    }

    @Override
    protected void handleJSONUpdateInBackground(final String json) {
        final Activity activity = getActivity();
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... nothing) {

                final ArrayList<Prize> newData = Prize.loadPrizesFromJSON(json);

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

    @Override
    public void onPositiveClick(ConfirmDialogFragment fragment) {

        // Start the intent chooser for this prize.
        int position = fragment.getArguments().getInt(CONFIRM_DIALOG_POSITION_KEY);
        Prize prize = mData.get(position);

        String[] receipients = { prize.contact };

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, receipients);

        String subject = String.format("Regarding the Hack The North prize"
                + " \"%s\"", prize.name);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);

        String title = getActivity().getString(R.string.send_email);

        DialogFragment dialog = IntentChooserDialogFragment.getInstance(intent,
                getActivity(), title);
        dialog.show(getFragmentManager(), INTENT_CHOOSER_DIALOG_FRAGMENT);
    }

    @Override
    public void onNegativeClick(ConfirmDialogFragment fragment) {
        // Do nothing
    }

    public static class PrizesFragmentAdapter extends ArrayAdapter<Prize> {
        private int mResource;
        private ArrayList<Prize> mData;
        private PrizesFragment mFragment;

        public PrizesFragmentAdapter(Context context, int resource,
                                     ArrayList<Prize> objects) {
            super(context, resource, objects);

            mResource = resource;
            mData = objects;
        }

        public void setFragment(PrizesFragment f) {
            mFragment = f;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                // If we don't have a view to reuse, inflate a new one.
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(mResource, parent, false);
            }

            // Get the data for this position
            final Prize prize = mData.get(position);

            // Compose email on tap if we have a contact email address
            if (prize.contact == null) {
                convertView.setOnClickListener(null);
            } else {
                convertView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        Resources res = view.getContext().getResources();
                        String title = res.getString(R.string.prize_dialog_title);
                        String message = String.format(res.getString(R.string.prize_dialog_message),
                                prize.name);
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
            }

            ((TextView)convertView.findViewById(R.id.prize_name))
                    .setText(prize.name);
            ((TextView)convertView.findViewById(R.id.prize_description))
                    .setText(prize.description);
            ((TextView)convertView.findViewById(R.id.prize_company))
                    .setText(prize.company);
            ((TextView)convertView.findViewById(R.id.prize_prizes))
                    .setText(getPrizesString(prize.prize));

            return convertView;
        }

        public int getCount() {
            return mData.size();
        }

        private String getPrizesString(ArrayList<String> prizesList) {

            if (prizesList == null || prizesList.size() == 0) {
                return null;
            }

            String prizes = " • " + prizesList.get(0);

            for (int i = 1; i < prizesList.size(); i++) {
                prizes += "\n • " + prizesList.get(i);
            }

            return prizes;
        }
    }
}
