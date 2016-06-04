package com.kaist.lts;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

public class ClientActivity extends AppCompatActivity {
    private static final String TAG = "[LTS][ClientActivity]";
    private static final int PICK_FILE_REQUEST = 1;
    public static int TIME_OUT = 0;
    static Context mContext;
    static FileHandler fh;
    static String selectedFilePath;
    private static PowerManager.WakeLock wakeLock;
    private static Handler mHandler;
    private static ProgressDialog dialog;
    private final int TOTAL_VIEW_PAGE_NUMBER = 3;
    /**
     * The {@link PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    private GoogleApiClient client;

    static private void showFileChooser(Activity activity) {
        Intent intent = new Intent();
        //sets the select file to all types of files
        intent.setType("file/*");
        //allows to select data and return it
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //starts new activity to select file and return data
        activity.startActivityForResult(Intent.createChooser(intent, "Choose File to Upload.."), PICK_FILE_REQUEST);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        mContext = this;
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        //Create Handler
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                //super.handleMessage(msg);
                if (msg.what == TIME_OUT) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            }
        };
        //My Info
        setContentView(R.layout.activity_client);
        setTitle(R.string.title_activity_client);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyUp");

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            moveTaskToBack(false);
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_FILE_REQUEST) {
                if (data == null) {
                    //no data present
                    return;
                }

                PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, ClientActivity.class.getSimpleName());
                wakeLock.acquire();

                Uri selectedFileUri = data.getData();
                selectedFilePath = FileHandler.getPath(this, selectedFileUri);
                Log.d(TAG, "Selected File Path:" + selectedFilePath);

/*                if (selectedFilePath != null && !selectedFilePath.isEmpty()) {
                    Toast.makeText(this, "upload file selected", Toast.LENGTH_SHORT).show();
                }*/
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_client, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        final int mode =ProfileManager.getUserMode(Session.GetInstance());
        if (mode == ProfileManager.USER_MODE.TRANSLATOR
                || mode == ProfileManager.USER_MODE.REVIEWER) {
            new Notifier(Notifier.Command.NEW_TRANSLATION_REQUEST, this.getApplicationContext());
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Client Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.kaist.lts/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Client Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.kaist.lts/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
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

        /**
         * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
         * one of the sections/tabs/pages.
         */
        static public void showUpload(Activity activity, View view, int pageNum) {
            final Activity ac = activity;
            if (pageNum == 2) {

                Log.d(TAG, "Show Upload items");
                ImageView attachIcon = (ImageView) view.findViewById(R.id.attach_icon);
                if (attachIcon != null) {
                    attachIcon.setVisibility(View.VISIBLE);
                    attachIcon.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            fh = new FileHandler();
                            showFileChooser(ac);

                        }
                    });
                }

                Button uploadButton = (Button) view.findViewById(R.id.upload_button);
                if (uploadButton != null) {
                    uploadButton.setVisibility(View.VISIBLE);
                    uploadButton.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (fh != null) {
                                Log.d(TAG, "Display progress bar");
                                dialog = ProgressDialog.show(mContext, "", "Uploading File...", true);
                                FileHandler.createUploadThread(mContext, selectedFilePath, wakeLock);
                                mHandler.sendEmptyMessageDelayed(TIME_OUT, 1000);
                            }
                        }
                    });
                }
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.d(TAG, "onCreateView");

            View rootView = inflater.inflate(R.layout.fragment_client, container, false);
            int pageViewNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            showUpload(getActivity(), rootView, pageViewNumber);
            //TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            //textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return TOTAL_VIEW_PAGE_NUMBER;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_fragment_noti);
                case 1:
                    return getString(R.string.title_fragment_req);
                case 2:
                    return getString(R.string.title_fragment_prof);
            }
            return null;
        }
    }
}
