package com.hackthenorth.android.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
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

import java.util.ArrayList;

public class PrizesFragment extends BaseListFragment {
    public static final String TAG = "UpdateListFragment";

    private ListView mListView;
    private ArrayList<Prize> mData = new ArrayList<Prize>();
    private PrizesFragmentAdapter mAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Create adapter
        mAdapter = new PrizesFragmentAdapter(activity, R.layout.prizes_list_item, mData);

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

    public static class PrizesFragmentAdapter extends ArrayAdapter<Prize> {
        private int mResource;
        private ArrayList<Prize> mData;

        public PrizesFragmentAdapter(Context context, int resource,
                                     ArrayList<Prize> objects) {
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
            final Prize prize = mData.get(position);

            // Compose email on tap
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String[] receipients = { prize.contact };

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("plain/text");
                    intent.putExtra(Intent.EXTRA_EMAIL, receipients);

                    String subject = String.format("Regarding the Hack The North prize"
                            + " \"%s\"", prize.name);
                    intent.putExtra(Intent.EXTRA_SUBJECT, subject);

                    String title = view.getContext()
                            .getString(R.string.send_email_to_prize_contact);
                    intent = Intent.createChooser(intent, title);
                    view.getContext().startActivity(intent);
                }
            });

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
