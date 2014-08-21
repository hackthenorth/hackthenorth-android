package com.hackthenorth.android.ui.settings;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import com.hackthenorth.android.R;
import com.hackthenorth.android.ui.component.TextView;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class SettingsActivity extends PreferenceActivity {

    private static final String SETTINGS_FRAGMENT_TAG = "settingsFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Resources resources = getResources();

        LayoutInflater inflater = (LayoutInflater)
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.titleview, null);

        TextView title = (TextView)view.findViewById(R.id.title);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/bebas_neue.ttf");
        title.setTypeface(tf);
        title.setText(resources.getString(R.string.settings_activity).toUpperCase());

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(view);

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintColor(resources.getColor(R.color.theme_primary));
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setNavigationBarTintColor(resources.getColor(R.color.theme_primary));

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
