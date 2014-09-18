package com.hackthenorth.android.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.support.v13.app.FragmentStatePagerAdapter;
import com.hackthenorth.android.R;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    private final String TAG = "FragmentStatePagerAdapter";

    public static final int UPDATES_POSITION = 0;
    public static final int SPONSORS_POSITION = 1;
    public static final int SCHEDULE_POSITION = 2;
    public static final int PRIZES_POSITION = 3;
    public static final int MENTORS_POSITION = 4;
    public static final int TEAM_POSITION = 5;

    public static final String UPDATES_TAG = "Updates";
    public static final String SPONSORS_TAG = "Sponsors";
    public static final String SCHEDULE_TAG = "Schedule";
    public static final String PRIZES_TAG = "Prizes";
    public static final String MENTORS_TAG = "Mentors";
    public static final String TEAM_TAG = "Team";

    private Fragment[] mFragments = new Fragment[6];
    private String[] mFragmentTitles;

    public ViewPagerAdapter(Context context, FragmentManager fm) {
        super(fm);

        Resources resources = context.getResources();
        mFragmentTitles = resources.getStringArray(R.array.fragment_titles);
    }

    @Override
    public Fragment getItem(int i) {
        if (mFragments[i] == null) {
            switch (i) {
                case UPDATES_POSITION:
                    mFragments[i] = new UpdatesFragment();
                    break;
                case SPONSORS_POSITION:
                    mFragments[i] = new SponsorsFragment();
                    break;
                case SCHEDULE_POSITION:
                    mFragments[i] = new ScheduleFragment();
                    break;
                case PRIZES_POSITION:
                    mFragments[i] = new PrizesFragment();
                    break;
                case MENTORS_POSITION:
                    mFragments[i] = new MentorsFragment();
                    break;
                case TEAM_POSITION:
                    mFragments[i] = new TeamFragment();
                    break;
            }
        }

        return mFragments[i];
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitles[position];
    }
}
