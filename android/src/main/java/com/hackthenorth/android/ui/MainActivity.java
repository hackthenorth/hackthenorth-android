package com.hackthenorth.android.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.devspark.robototextview.util.RobotoTypefaceManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.hackthenorth.android.R;
import com.hackthenorth.android.base.BaseActivity;
import com.hackthenorth.android.framework.GCMRegistrationManager;
import com.hackthenorth.android.ui.component.ExplodingImageView;
import com.hackthenorth.android.ui.component.PagerTitleStrip;
import com.hackthenorth.android.ui.component.TextView;
import com.hackthenorth.android.ui.mentor.MentorsFragment;
import com.hackthenorth.android.ui.settings.SettingsActivity;
import com.hackthenorth.android.util.Units;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class MainActivity extends BaseActivity implements AbsListView.OnScrollListener {

    private static final String TAG = "MainActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private ViewPagerAdapter mViewPagerAdapter;
    private PagerTitleStrip mViewPagerTabs;
    private ViewPager mViewPager;

    // ActionBar menu items
    private MenuItem mSettingsMenuItem;

    // Action bar state
    private static final int ACTION_BAR_STATE_NORMAL = 0;
    private static final int ACTION_BAR_STATE_SEARCH = 1;

    private int actionBarState;

    // Search animation state
    private static final int ACTION_NONE = 0;
    private static final int ACTION_APPEAR = 1;
    private static final int ACTION_DISAPPEAR = 2;

    private int currentAction = ACTION_NONE;
    private int nextAction = ACTION_NONE;

    private static final int DURATION = 200;

    private ExplodingImageView mSearchButton;
    private ExplodingImageView mCancelButton;
    private TextView mTitle;
    private EditText mSearchBox;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Resources resources = getResources();

        LayoutInflater inflater = (LayoutInflater)
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.titleview, null);

        mTitle = (TextView) view.findViewById(R.id.title);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/bebas_neue.ttf");
        mTitle.setTypeface(tf);
        mTitle.setText(resources.getString(R.string.app_name).toUpperCase());

        mSearchBox = (EditText)view.findViewById(R.id.searchBox);
        mSearchBox.setOnEditorActionListener(new android.widget.TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(android.widget.TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (mViewPagerAdapter != null) {
                        Fragment f = mViewPagerAdapter.getItem(mViewPager.getCurrentItem());
                        if (f instanceof MentorsFragment) {
                            MentorsFragment mentorsFragment = (MentorsFragment)f;
                            mentorsFragment.getAdapter().query("ayyyy lmao");
                            return true;
                        }
                    }
                }
                return false;
            }
        });
        mSearchButton = (ExplodingImageView)view.findViewById(R.id.searchButton);
        mCancelButton = (ExplodingImageView)view.findViewById(R.id.cancelButton);

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearchBarAction(ACTION_APPEAR);
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearchBarAction(ACTION_DISAPPEAR);
            }
        });

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(view);

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintColor(resources.getColor(R.color.theme_primary));
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setNavigationBarTintColor(resources.getColor(R.color.theme_primary));

        mViewPagerAdapter = new ViewPagerAdapter(this, getFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mViewPagerAdapter);

        mViewPagerTabs = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
        mViewPagerTabs.setTypeface(RobotoTypefaceManager.obtainTypeface(
                this, RobotoTypefaceManager.Typeface.ROBOTO_REGULAR), 0);
        mViewPagerTabs.setViewPager(mViewPager);
        mViewPagerTabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int i, float v, int i2) { }
            @Override public void onPageScrollStateChanged(int i) {
            }

            @Override
            public void onPageSelected(int i) {
                // When the fragment page changes, reload the options menu.
                updateOptionsMenu();
            }
        });

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        // Save menu items
        mSettingsMenuItem = menu.findItem(R.id.action_settings);

        if (mViewPager == null || !searchable(mViewPager.getCurrentItem())) {
            mSearchButton.setVisibility(View.GONE);
        }

        // Mmmmmm boilerplate
        mSettingsMenuItem.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { onOptionsItemSelected(mSettingsMenuItem); }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void updateOptionsMenu() {

        // Show / hide the search button
        switch (mViewPager.getCurrentItem()) {
            case ViewPagerAdapter.UPDATES_POSITION:
            case ViewPagerAdapter.SCHEDULE_POSITION:
            case ViewPagerAdapter.PRIZES_POSITION:
            case ViewPagerAdapter.TEAM_POSITION:
                if (actionBarState == ACTION_BAR_STATE_NORMAL) {
                    mSearchButton.setExplodingVisibility(View.GONE);
                } else {
                    startSearchBarAction(ACTION_DISAPPEAR);
                }
                break;
            case ViewPagerAdapter.MENTORS_POSITION:
                if (actionBarState == ACTION_BAR_STATE_NORMAL) {
                    mSearchButton.setExplodingVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onBackPressed() {
        // If we are in a searching state, go back to the normal state.
        // Otherwise just close the activity as usual.
        if (actionBarState == ACTION_BAR_STATE_SEARCH) {
            startSearchBarAction(ACTION_DISAPPEAR);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Returns true iff the position corresponds to a fragment that is searchable.
     */
    private boolean searchable(int position) {
        return position == ViewPagerAdapter.MENTORS_POSITION;
    }

    private void startSearchBarAction(int action) {
        if (action == ACTION_APPEAR && actionBarState == ACTION_BAR_STATE_NORMAL) {
            if (currentAction == ACTION_NONE) {
                // Start the search bar animation!
                currentAction = ACTION_APPEAR;
                searchBarAppear();
            } else if (currentAction == ACTION_APPEAR && nextAction == ACTION_DISAPPEAR) {
                // Cancel the pending disappear action
                nextAction = ACTION_NONE;
            } else if (currentAction == ACTION_DISAPPEAR) {
                // Oh, rats! We have an appear action but we already started disappearing.
                // Queue up the appear action.
                nextAction = ACTION_APPEAR;
            }
        } else if (action == ACTION_DISAPPEAR && actionBarState == ACTION_BAR_STATE_SEARCH) {
            if (currentAction == ACTION_NONE) {
                // Disappear!
                currentAction = ACTION_DISAPPEAR;
                searchBarDisappear();
            } else if (currentAction == ACTION_DISAPPEAR && nextAction == ACTION_DISAPPEAR) {
                // Cancel the pending appear action
                nextAction = ACTION_NONE;
            } else if (currentAction == ACTION_APPEAR) {
                // We're currently appearing, so we have to disappear when we finish.
                nextAction = ACTION_DISAPPEAR;
            }
        }
    }

    private void searchBarAppear() {

        //
        // Icon / Title / Settings button disappear animation
        //
        actionBarState = ACTION_BAR_STATE_SEARCH;

        AlphaAnimation alpha = new AlphaAnimation(1f, 0f);
        alpha.setInterpolator(new DecelerateInterpolator());
        alpha.setDuration(DURATION);
        alpha.setFillAfter(true);

        TranslateAnimation translate = new TranslateAnimation(
                Animation.ABSOLUTE, 0f,
                Animation.ABSOLUTE, 0f,
                Animation.ABSOLUTE, 0f,
                Animation.RELATIVE_TO_SELF, -0.5f);
        translate.setInterpolator(new DecelerateInterpolator());
        translate.setDuration(DURATION);

        AnimationSet set = new AnimationSet(false);
        set.addAnimation(translate);
        set.addAnimation(alpha);
        set.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                // Don't ever have the title be *gone*, because then we get issues with
                // the custom view clipping child bounds (it's the parent of the custom
                // view that does it, so we can't really fix it nicely).
                mTitle.setVisibility(View.INVISIBLE);
                mSettingsMenuItem.getActionView().setVisibility(View.GONE);
            }
        });

        mSettingsMenuItem.getActionView().startAnimation(set);
        mTitle.startAnimation(set);

        //
        // Search icon animation
        // Big comment because this is a lot of code
        //

        // This is the margin that the action bar icon has from the left edge
        // of the screen.
        int leftMargin = Units.dpToPx(this, 12);
        int[] xy = new int[2];
        mSearchButton.getLocationOnScreen(xy);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)
                mSearchButton.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);
        mSearchButton.setLayoutParams(params);

        // We adjust the left padding of the icon so that it fits nicely on the
        // left side of the action bar.
        int adjustment = Units.dpToPx(this, 4);
        mSearchButton.setPadding(mSearchButton.getPaddingLeft() - leftMargin - adjustment,
                mSearchButton.getPaddingTop(), mSearchButton.getPaddingRight(),
                mSearchButton.getPaddingBottom());

        translate = new TranslateAnimation(
                Animation.ABSOLUTE, xy[0] - leftMargin + adjustment,
                Animation.ABSOLUTE, 0f,
                Animation.ABSOLUTE, 0f,
                Animation.ABSOLUTE, 0f);
        translate.setInterpolator(new AccelerateDecelerateInterpolator());
        translate.setDuration(3 * DURATION / 2);
        translate.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) { }
            @Override public void onAnimationRepeat(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {

                // This is a little more subtle than other parts of this code. If the next
                // action is DISAPPEAR, we don't want to show the cancel button at all.
                // However, we also have to check the state again, or else we can get into
                // a bad state if the user cancels after the cancel button starts animating.
                if (nextAction == ACTION_DISAPPEAR) {
                    currentAction = nextAction;
                    nextAction = ACTION_NONE;
                    searchBarDisappear();
                } else {

                    // Show the cancel button, then do all the state stuff and show the
                    // keyboard. We do it this way because we get animation jank if we
                    // show the keyboard at the same time as our animations.
                    mCancelButton.setExplodingVisibility(View.VISIBLE, new AnimationListener() {
                        @Override public void onAnimationStart(Animation animation) {  }
                        @Override public void onAnimationRepeat(Animation animation) {  }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            currentAction = ACTION_NONE;
                            if (nextAction == ACTION_DISAPPEAR) {
                                currentAction = nextAction;
                                nextAction = ACTION_NONE;
                                searchBarDisappear();
                            } else {
                                mSearchBox.setVisibility(View.VISIBLE);
                                if (mSearchBox.requestFocus()) {
                                    InputMethodManager imm = (InputMethodManager)
                                            getSystemService(Service.INPUT_METHOD_SERVICE);
                                    imm.showSoftInput(mSearchBox, 0);
                                }
                            }
                        }
                    });
                }
            }
        });

        mSearchButton.startAnimation(translate);
    }

    private void searchBarDisappear() {

        mCancelButton.setVisibility(View.GONE);

        // The user may have switched to a non-searchable fragment, in which case we
        // shouldn't show the search menu item.
        boolean showSearchButton = searchable(mViewPager.getCurrentItem());

        mTitle.setVisibility(View.VISIBLE);
        mSearchButton.setVisibility(View.VISIBLE);
        mSettingsMenuItem.getActionView().setVisibility(View.VISIBLE);
        mSearchBox.setVisibility(View.GONE);

        actionBarState = ACTION_BAR_STATE_NORMAL;

        // Set up animations for the title and the settings button to disappear
        AlphaAnimation alpha = new AlphaAnimation(0f, 1f);
        alpha.setInterpolator(new AccelerateInterpolator());
        alpha.setDuration(DURATION);
        alpha.setFillAfter(true);

        TranslateAnimation translate = new TranslateAnimation(
                Animation.ABSOLUTE, 0f,
                Animation.ABSOLUTE, 0f,
                Animation.RELATIVE_TO_SELF, -1f,
                Animation.ABSOLUTE, 0f);
        translate.setInterpolator(new AccelerateInterpolator());

        AnimationSet set = new AnimationSet(false);

        if (showSearchButton) {
            translate.setDuration(DURATION / 2);
            set.setStartOffset(DURATION);
        } else {
            translate.setDuration(3 * DURATION / 2);
            alpha.setDuration(3 * DURATION / 2);
            // Otherwise, we should handle all the state changes in the listener here.
            set.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation animation) { }
                @Override public void onAnimationRepeat(Animation animation) { }
                @Override
                public void onAnimationEnd(Animation animation) {
                    currentAction = ACTION_NONE;
                    if (nextAction == ACTION_APPEAR) {
                        currentAction = nextAction;
                        nextAction = ACTION_NONE;
                        searchBarAppear();
                    } else {
                        // Hide the keyboard
                        InputMethodManager imm = (InputMethodManager)
                                getSystemService(Service.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(mSearchBox.getWindowToken(), 0);
                    }
                }
            });
        }

        set.addAnimation(translate);
        set.addAnimation(alpha);

        mSettingsMenuItem.getActionView().startAnimation(set);
        mTitle.startAnimation(set);

        //
        // Search icon animation
        // Big comment because this is a lot of code
        //

        // If we're going to show the search button...
        if (showSearchButton) {
            // First, let's change the relative layout things back.
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)
                    mSearchButton.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
            mSearchButton.setLayoutParams(params);

            // We adjust the left padding of the icon so that it fits nicely on the
            // left side of the action bar.
            final int adjustment = Units.dpToPx(this, 4);
            // This is the margin that the action bar icon has from the left edge
            // of the screen.
            final int leftMargin = Units.dpToPx(this, 12);
            mSearchButton.setPadding(mSearchButton.getPaddingLeft() + leftMargin + adjustment,
                    mSearchButton.getPaddingTop(), mSearchButton.getPaddingRight(),
                    mSearchButton.getPaddingBottom());

            // We use a predraw listener here because we need to measure where the icon
            // *would* be if it were aligned to the right side of the parent before we can
            // animate it to that position.
            mSearchButton.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    int[] xy = new int[2];

                    mSearchButton.getLocationOnScreen(xy);

                    TranslateAnimation translate = new TranslateAnimation(
                            Animation.ABSOLUTE, -xy[0] + adjustment,
                            Animation.ABSOLUTE, 0f,
                            Animation.ABSOLUTE, 0f,
                            Animation.ABSOLUTE, 0f);
                    translate.setInterpolator(new AccelerateDecelerateInterpolator());
                    translate.setDuration(3 * DURATION / 2);
                    mSearchButton.startAnimation(translate);

                    translate.setAnimationListener(new Animation.AnimationListener() {
                        @Override public void onAnimationStart(Animation animation) { }
                        @Override public void onAnimationRepeat(Animation animation) { }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            currentAction = ACTION_NONE;
                            if (nextAction == ACTION_APPEAR) {
                                currentAction = nextAction;
                                nextAction = ACTION_NONE;
                                searchBarAppear();
                            } else {
                                // Hide the keyboard
                                InputMethodManager imm = (InputMethodManager)
                                        getSystemService(Service.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(mSearchBox.getWindowToken(), 0);
                            }
                        }
                    });

                    // Remove the pre draw listener---we only want this to happen once.
                    mSearchButton.getViewTreeObserver().removeOnPreDrawListener(this);
                    return false;
                }
            });

        } else {
            // This is the case where we don't want to show the search button when the
            // animation is over. We'll just fade it out to the bottom, in a similar way
            // that the title etc. are fading in from the top.
            alpha = new AlphaAnimation(1f, 0f);
            alpha.setInterpolator(new AccelerateInterpolator());
            alpha.setDuration(3 * DURATION / 2);
            alpha.setFillAfter(true);

            translate = new TranslateAnimation(
                    Animation.ABSOLUTE, 0f,
                    Animation.ABSOLUTE, 0f,
                    Animation.ABSOLUTE, 0f,
                    Animation.RELATIVE_TO_SELF, 1f);
            translate.setInterpolator(new AccelerateInterpolator());
            translate.setDuration(3 * DURATION / 2);

            set = new AnimationSet(false);
            set.addAnimation(translate);
            set.addAnimation(alpha);
            final Activity activity = this;
            set.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation animation) {  }
                @Override public void onAnimationRepeat(Animation animation) {  }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mSearchButton.setVisibility(View.GONE);

                    // We adjust the left padding of the icon so that it fits nicely on the
                    // left side of the action bar.
                    final int adjustment = Units.dpToPx(activity, 4);
                    // This is the margin that the action bar icon has from the left edge
                    // of the screen.
                    final int leftMargin = Units.dpToPx(activity, 12);
                    mSearchButton.setPadding(mSearchButton.getPaddingLeft() + leftMargin + adjustment,
                            mSearchButton.getPaddingTop(), mSearchButton.getPaddingRight(),
                            mSearchButton.getPaddingBottom());

                    // Lastly, change the relative layout things back.
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)
                            mSearchButton.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
                    mSearchButton.setLayoutParams(params);
                }
            });

            mSearchButton.startAnimation(set);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (searchable(mViewPager.getCurrentItem())) {
            startSearchBarAction(ACTION_DISAPPEAR);
        }
    }

    @Override public void onScroll(AbsListView view, int firstVisibleItem,
                                   int visibleItemCount, int totalItemCount) {}
}
