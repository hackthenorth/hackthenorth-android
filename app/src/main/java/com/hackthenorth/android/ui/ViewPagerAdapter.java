package com.hackthenorth.android.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.support.v13.app.FragmentStatePagerAdapter;
import com.hackthenorth.android.R;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    private final String TAG = "FragmentStatePagerAdapter";


    private Fragment[] mFragments = new Fragment[5];
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
                case 0:
                    mFragments[i] = new UpdatesFragment();
                    break;
                case 1:
                    mFragments[i] = new ScheduleFragment();
                    break;
                case 2:
                    mFragments[i] = new PrizesFragment();
                    break;
                case 3:
                    mFragments[i] = new MentorsFragment();
                    break;
                case 4:
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
