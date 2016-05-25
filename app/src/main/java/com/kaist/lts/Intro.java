package com.kaist.lts;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class Intro extends AppCompatActivity {
    private static final String TAG = "[LTS][Intro]";
    private static final int TOTAL_VIEW_PAGE_NUMBER = 3;
    static private Context mContext;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        //showStartUpButton();
    }
/*
    public void showStartUpButton() {
        Log.d(TAG, "showStartUpButton");

        Button startButton = (Button) findViewById(R.id.start_button);
        if (startButton == null) {
            throw new AssertionError();
        }
        //startButton.setVisibility(View.VISIBLE);
        startButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO : click event
                SharedPreferences prefs = getSharedPreferences("lts", MODE_PRIVATE);
                SharedPreferences.Editor prefEditor = prefs.edit();
                prefEditor.putBoolean("startup", true);
                prefEditor.apply();

                Intent intent = new Intent();
                intent.setClassName("com.kaist.lts", "com.kaist.lts.MainActivity");
                mContext.startActivity(intent);
            }
        });
    }*/

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //return super.onKeyDown(keyCode, event);
        Log.d(TAG, "onKeyUp");

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            moveTaskToBack(true);
            finish();
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        static public void showStartUpButton(View view) {
            Log.d(TAG, "showStartUpButton");

            Button startButton = (Button) view.findViewById(R.id.start_button);
            if (startButton == null) {
                throw new AssertionError();
            }
            startButton.setVisibility(View.VISIBLE);
            startButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO : click event
                    SharedPreferences prefs = mContext.getSharedPreferences("lts", MODE_PRIVATE);
                    SharedPreferences.Editor prefEditor = prefs.edit();
                    prefEditor.putBoolean("startup", true);
                    prefEditor.apply();

                    Intent intent = new Intent();
                    intent.setClassName("com.kaist.lts", "com.kaist.lts.MainActivity");
                    mContext.startActivity(intent);
                }
            });
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_intro, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            int pageViewNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            if (pageViewNumber == TOTAL_VIEW_PAGE_NUMBER) {
                showStartUpButton(rootView);
            }
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            Log.d(TAG, "getItem");

            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            Log.d(TAG, "getCount");
            return TOTAL_VIEW_PAGE_NUMBER;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Log.d(TAG, "getPageTitle");
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }


    }
}
