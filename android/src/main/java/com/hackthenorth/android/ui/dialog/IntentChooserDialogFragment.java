package com.hackthenorth.android.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.hackthenorth.android.R;
import com.hackthenorth.android.ui.component.TextView;

import java.util.ArrayList;
import java.util.List;

public class IntentChooserDialogFragment extends DialogFragment {

    public static final String TITLE_KEY = "title";
    public static final String INTENT_KEY = "intent";

    public static IntentChooserDialogFragment getInstance(Intent intent, Context context,
                                                          String title)  {

        IntentChooserDialogFragment f = new IntentChooserDialogFragment();

        Bundle args = new Bundle();
        args.putString(TITLE_KEY, title);
        args.putParcelable(INTENT_KEY, intent);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_material_intent_chooser, null);

        // Set title
        String title = getArguments().getString(TITLE_KEY);
        ((TextView)view.findViewById(R.id.title)).setText(title);

        // Set up the adapter with the list of ResolveInfo as the data
        PackageManager manager = getActivity().getPackageManager();
        Intent intent = getArguments().getParcelable(INTENT_KEY);
        List<ResolveInfo> infos = manager.queryIntentActivities(intent, 0);

        IntentChooseDialogFragmentAdapter adapter = new IntentChooseDialogFragmentAdapter(
                getActivity(), R.layout.intent_chooser_list_item, infos, intent, this);
        ((ListView)view.findViewById(android.R.id.list))
                .setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }

    public static class IntentChooseDialogFragmentAdapter extends ArrayAdapter<ResolveInfo> {

        List<ResolveInfo> mData;
        Intent mIntent;
        DialogFragment mFragment;
        Context mContext;
        int mResource;

        public IntentChooseDialogFragmentAdapter(Context context, int resource,
                                                 List<ResolveInfo> data, Intent intent,
                                                 DialogFragment fragment) {
            super(context, resource, data);

            mContext = context;
            mResource = resource;
            mData = data;
            mIntent = intent;
            mFragment = fragment;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater)
                        mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(mResource, null);
            }

            PackageManager manager = mContext.getPackageManager();

            final ResolveInfo info = mData.get(position);

            ((TextView)convertView.findViewById(R.id.activityName))
                    .setText(info.activityInfo.loadLabel(manager));

            Drawable drawable = info.activityInfo.loadIcon(manager);
            ((ImageView)convertView.findViewById(R.id.image))
                    .setImageDrawable(drawable);

            // Set click handler
            final DialogFragment dialog = mFragment;
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mIntent.setPackage(info.activityInfo.packageName);
                    mContext.startActivity(mIntent);

                    dialog.dismiss();
                }
            });

            return convertView;
        }
    }
}
