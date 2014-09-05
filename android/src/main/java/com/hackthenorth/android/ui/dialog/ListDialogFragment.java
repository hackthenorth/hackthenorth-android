package com.hackthenorth.android.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
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

public class ListDialogFragment extends DialogFragment {

    private static String TITLE_KEY = "title";
    private static String CONTENT_KEY = "content";
    private static String ITEMS_KEY = "items";
    private static String RES_IDS_KEY = "resIds";
    private static String CANCEL_BUTTON_TEXT_KEY = "cancelButtonText";

    public static interface ListDialogFragmentListener {
        public void onItemClick(ListDialogFragment fragment, int position);
        public void onCancelButtonClick(ListDialogFragment fragment);
    }

    /**
     * Do not use this method! It is used internally to stay connected to the listener fragment. You
     * will break the listener if you use it after calling ListDialogFragment#getInstance().
     */
    @Deprecated
    @Override
    public void setTargetFragment(Fragment f, int req) {
        super.setTargetFragment(f, req);
    }

    /**
     * Temporary note: this works a little differently from ConfirmDialogFragment. I've made updates
     * to the API, but don't have time to update ConfirmDialogFragment. Maybe we can use a base class
     * for the LCDs of the listeners?
     *
     * @param listener The listener fragment for this dialog. It will be the fragment that
     *                 implements all the callbacks for item clicks, etc. Can be null.
     * @param title Title text. Can be null.
     * @param content Text to be displayed underneath the title and above the list. Can be null.
     * @param items ArrayList of items. Can not be null.
     * @param resIds ArrayList of resIds. Will be loaded as images beside the items. Can not be null.
     * @param cancelButtonText Cancel button text. If null, then there will be no cancel button.
     * @param <T> The fragment / listener type; extends fragment and implements
     *           ListDialogFragmentListener.
     * @return
     */
    public static <T extends Fragment & ListDialogFragmentListener>
    ListDialogFragment getInstance(T listener, String title, String content,
                                   ArrayList<String> items, ArrayList<Integer> resIds,
                                   String cancelButtonText) {

        ListDialogFragment f = new ListDialogFragment();

        // Save the listener as the target fragment. This relationship will be maintained across
        // rotations!

        // Note: Only deprecated here to help the users of this class know that they shouldn't
        // override the target fragment, since we're using it here.
        f.setTargetFragment(listener, 0);

        // Put the data into the fragment arguments.
        Bundle args = new Bundle();
        args.putString(TITLE_KEY, title);
        args.putString(CONTENT_KEY, content);
        args.putStringArrayList(ITEMS_KEY, items);
        args.putIntegerArrayList(RES_IDS_KEY, resIds);
        args.putString(CANCEL_BUTTON_TEXT_KEY, cancelButtonText);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        Bundle args = getArguments();
        View view = inflater.inflate(R.layout.dialog_list, null);

        // Get the listener for this dialog out of the static map.
        final ListDialogFragmentListener listener = (ListDialogFragmentListener)getTargetFragment();

        // Set up the dialog views
        String title = args.getString(TITLE_KEY, null);
        TextView titleText = (TextView)view.findViewById(R.id.title);
        if (title != null) {
            titleText.setText(title);
        } else {
            titleText.setVisibility(View.GONE);
        }

        String content = args.getString(CONTENT_KEY, null);
        TextView contentText = (TextView)view.findViewById(R.id.content);
        if (content != null) {
            contentText.setText(content);
        } else {
            contentText.setVisibility(View.GONE);
        }

        // Set up the list: Data, adapter, listview
        ArrayList<String> items = args.getStringArrayList(ITEMS_KEY);
        ArrayList<Integer> resIds = args.getIntegerArrayList(RES_IDS_KEY);
        ArrayList<ListDialogItem> data = new ArrayList<ListDialogItem>(items.size());
        for (int i = 0; i < items.size(); i++ ) {
            ListDialogItem item = new ListDialogItem();
            item.text = items.get(i);
            item.resId = resIds.get(i);
            data.add(item);
        }

        ListDialogFragmentAdapter adapter = new ListDialogFragmentAdapter(
                view.getContext(), R.layout.list_dialog_item, data, this, listener);
        ListView list = (ListView)view.findViewById(android.R.id.list);
        list.setAdapter(adapter);

        final ListDialogFragment f = this;
        String cancelButtonText = args.getString(CANCEL_BUTTON_TEXT_KEY, null);
        TextView cancelButton = (TextView)view.findViewById(R.id.cancelButton);
        if (cancelButtonText != null) {
            cancelButton.setText(cancelButtonText);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onCancelButtonClick(f);
                    }
                }
            });
        } else {
            cancelButton.setVisibility(View.GONE);
        }

        builder.setView(view);
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private static class ListDialogItem {
        public String text;
        public int resId;
    }

    public static class ListDialogFragmentAdapter extends ArrayAdapter<ListDialogItem> {

        List<ListDialogItem> mData;
        ListDialogFragment mFragment;
        ListDialogFragmentListener mListener;
        Context mContext;
        int mResource;

        public ListDialogFragmentAdapter(Context context, int resource,
                                         ArrayList<ListDialogItem> data,
                                         ListDialogFragment fragment,
                                         ListDialogFragmentListener listener) {
            super(context, resource, data);

            mContext = context;
            mResource = resource;
            mData = data;
            mFragment = fragment;
            mListener = listener;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater)
                        mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(mResource, null);
            }

            ListDialogItem item = mData.get(position);

            ((ImageView)convertView.findViewById(R.id.image))
                    .setImageResource(item.resId);
            ((TextView)convertView.findViewById(R.id.text))
                    .setText(item.text);

            final ListDialogFragment dialog = mFragment;
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onItemClick(mFragment, position);
                    }
                }
            });

            return convertView;
        }
    }
}
