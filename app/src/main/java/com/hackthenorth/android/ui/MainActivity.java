package com.hackthenorth.android.ui;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.devspark.robototextview.util.RobotoTypefaceManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.hackthenorth.android.R;
import com.hackthenorth.android.base.BaseActivity;
import com.hackthenorth.android.framework.GCMRegistrationManager;
import com.hackthenorth.android.framework.VisibilityManager;
import com.hackthenorth.android.ui.component.PagerTitleStrip;
import com.hackthenorth.android.ui.component.TextView;
import com.hackthenorth.android.ui.settings.SettingsActivity;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private Resources mResources;
    private ActionBar mActionBar;
    private LayoutInflater mInflater;
    private TextView mTitle;

    private ViewPagerAdapter mViewPagerAdapter;
    private PagerTitleStrip mViewPagerTabs;
    private ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPagerAdapter = new ViewPagerAdapter(this, getFragmentManager());

        mResources = getResources();

        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.titleview, null);
        mTitle = (TextView) view.findViewById(R.id.title);
        mTitle.setText(getTitle());

        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setCustomView(view);

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintColor(mResources.getColor(R.color.theme_primary));
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setNavigationBarTintColor(mResources.getColor(R.color.theme_primary));

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mViewPagerAdapter);

        mViewPagerTabs = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
        mViewPagerTabs.setTypeface(RobotoTypefaceManager.obtainTypeface(
                this, RobotoTypefaceManager.Typeface.ROBOTO_REGULAR), 0);
        mViewPagerTabs.setViewPager(mViewPager);

        if (checkPlayServices()) {
            if (GCMRegistrationManager.getRegistrationId(this) == null) {
                // Register with GCM
                GCMRegistrationManager.registerInBackground(this);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPlayServices();
        VisibilityManager.activityResumed();
    }

    @Override
    public void onPause() {
        super.onPause();
        VisibilityManager.activityPaused();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence chars) {
        mTitle.setText(chars);
        super.setTitle(chars);
    }

    // Copied from the GCM Client tutorial [1].
    // [1]: https://developer.android.com/google/gcm/client.html
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(this, "You must have Play Services to use this app.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }
}