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
}
