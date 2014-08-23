package com.hackthenorth.android.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.hackthenorth.android.R;
import com.hackthenorth.android.ui.component.TextView;

public class ConfirmDialogFragment extends DialogFragment {

    private static String TITLE_KEY = "title";
    private static String CONTENT_KEY = "content";
    private static String POSITIVE_TEXT_KEY = "positive_text";
    private static String NEGATIVE_TEXT_KEY = "negative_text";

    public static interface ConfirmDialogFragmentListener {
        public void onPositiveClick(ConfirmDialogFragment fragment);
        public void onNegativeClick(ConfirmDialogFragment fragment);
    }

    public static ConfirmDialogFragment getInstance(String title, String content,
                                                    String positiveText,
                                                    String negativeText) {

        ConfirmDialogFragment f = new ConfirmDialogFragment();

        // Put the data into the fragment arguments
        Bundle args = new Bundle();
        args.putString(TITLE_KEY, title);
        args.putString(CONTENT_KEY, content);
        args.putString(POSITIVE_TEXT_KEY, positiveText);
        args.putString(NEGATIVE_TEXT_KEY, negativeText);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        Bundle args = getArguments();
        View view = inflater.inflate(R.layout.dialog_material_confirm, null);

        // Get the listener for this dialog out of the static map.
        ConfirmDialogFragmentListener t_listener;
        try {
            t_listener = (ConfirmDialogFragmentListener)getTargetFragment();
        } catch (ClassCastException e) {
            throw new RuntimeException("TargetFragment must implement " +
                    "ConfirmDialogFragmentListener interface.");
        }
        final ConfirmDialogFragmentListener listener = t_listener;

        // Set up the dialog views
        ((TextView)view.findViewById(R.id.title))
                .setText(args.getString(TITLE_KEY));
        ((TextView)view.findViewById(R.id.content))
                .setText(args.getString(CONTENT_KEY));

        // Hook up the buttons to the listener's callbacks
        final ConfirmDialogFragment dialog = this;
        TextView positiveButton = (TextView)view.findViewById(R.id.positiveButton);
        positiveButton.setText(args.getString(POSITIVE_TEXT_KEY));
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onPositiveClick(dialog);
                dialog.dismiss();
            }
        });

        TextView negativeButton = (TextView)view.findViewById(R.id.negativeButton);
        negativeButton.setText(args.getString(NEGATIVE_TEXT_KEY));
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onNegativeClick(dialog);
                dialog.dismiss();
            }
        });
        ((TextView)view.findViewById(R.id.negativeButton))
                .setText(args.getString(NEGATIVE_TEXT_KEY));

        builder.setView(view);
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
