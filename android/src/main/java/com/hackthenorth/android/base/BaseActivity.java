package com.hackthenorth.android.base;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.hackthenorth.android.HackTheNorthApplication;
import com.hackthenorth.android.R;
import com.hackthenorth.android.framework.VisibilityManager;
import com.hackthenorth.android.ui.component.TextView;
import com.readystatesoftware.systembartint.SystemBarTintManager;

/**
 * A base class for activities. This is used to determine if our app is in the foreground.
 * If you make a new activity, make sure to extend from this base class, or else
 * implement the operations that are given here.
 *
 * Note: See [1] for reference.
 * [1]: http://stackoverflow.com/questions/18038399/how-to-check-if-activity-is-in-foreground-or-in-visible-background
 */
public class BaseActivity extends Activity {

    protected TextView mTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Resources resources = getResources();

        LayoutInflater inflater = (LayoutInflater)
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.titleview, null);

        mTitle = (TextView) view.findViewById(R.id.title);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/bebas_neue.ttf");
        mTitle.setTypeface(tf);
        mTitle.setText(resources.getString(R.string.app_name).toUpperCase());

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(view);

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintColor(resources.getColor(R.color.theme_primary));
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setNavigationBarTintColor(resources.getColor(R.color.theme_primary));
    }

    @Override
    public void onResume() {
        super.onResume();
        VisibilityManager.activityResumed();
    }

    @Override
    public void onPause() {
        super.onPause();
        VisibilityManager.activityPaused();
    }

    @Override
    public void setTitle(CharSequence chars) {
        mTitle.setText(chars);
        super.setTitle(chars);
    }
}
