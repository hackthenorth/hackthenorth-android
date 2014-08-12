package com.hackthenorth.android.ui;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.hackthenorth.android.HackTheNorthApplication;
import com.hackthenorth.android.R;
import com.hackthenorth.android.framework.GCMRegistrationManager;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class MainActivity extends SlidingFragmentActivity {

    private static final String TAG = "MainActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private SlidingMenu mSlidingMenu;
    private Resources mResources;
    private Fragment mContent;
    private Fragment mMenu;
    private ActionBar mActionBar;
    private LayoutInflater mInflater;
    private TextView mTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        if (savedInstanceState != null) {
            mContent = getFragmentManager().getFragment(savedInstanceState, "content");
        } else {
            mContent = InfoListFragment.newInstance("updates");
        }

        mSlidingMenu = getSlidingMenu();
        mSlidingMenu.setShadowDrawable(R.drawable.shadow);
        mSlidingMenu.setShadowWidth(mResources.getDimensionPixelSize(R.dimen.navigation_drawer_shadow_width));
        mSlidingMenu.setBehindOffset(mResources.getDimensionPixelSize(R.dimen.navigation_drawer_offset));
        mSlidingMenu.setBehindScrollScale(0.5f);
        mSlidingMenu.setFadeDegree(0.5f);
        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        setSlidingActionBarEnabled(true);

        setContentView(R.layout.content_frame);
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, mContent)
                .commit();

        setBehindContentView(R.layout.menu_frame);

        if (checkPlayServices()) {
            if (GCMRegistrationManager.getRegistrationId(this) == null) {
                // Register with GCM
                GCMRegistrationManager.registerInBackground(this);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();

        // Close notifications here
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(HackTheNorthApplication.NOTIFICATIONS_ID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
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