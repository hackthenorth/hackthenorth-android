package com.hackthenorth.android.ui.settings;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import com.hackthenorth.android.R;

public class SettingsActivity extends PreferenceActivity {

    private static final String SETTINGS_FRAGMENT_TAG = "settingsFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Gives the back button in the action bar
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Fragment settingsFragment = new SettingsFragment();
        getFragmentManager().beginTransaction()
                .add(android.R.id.content, settingsFragment, SETTINGS_FRAGMENT_TAG)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }
}
