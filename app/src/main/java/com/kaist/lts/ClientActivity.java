package com.kaist.lts;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

public class ClientActivity extends AppCompatActivity {
    static final String TAG = "[LTS][ClientActivity]";
    static final int PAGE_NUM_NOTIFY = 1;
    static final int PAGE_NUM_REQUEST = 2;
    static final int PAGE_NUM_PROFILE = 3;
    static final int PICK_FILE_REQUEST = 1;
    static public int TIME_OUT = 0;

    static Context mContext;
    static FileHandler fh;
    static Spinner type;
    static Spinner level;
    static Spinner payment;
    static Spinner lang;
    static String dueDate;
    static String selectedFilePath;

    static private PowerManager.WakeLock wakeLock;
    static private Handler mHandler;
    static private ProgressDialog dialog;
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

    private static void createUploadItems(View view, final Activity ac) {
        Log.d(TAG, "Show upload items");
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

                        //Generate json format
                        JSONObject req = new JSONObject();

                        //req.put("id", Login.id);
                        req.put("target_language", lang.getSelectedItem());
                        req.put("doc_type", type.getSelectedItem());
                        req.put("level", level.getSelectedItem());
                        req.put("cost", payment.getSelectedItem());
                        req.put("due_date", dueDate);
                        req.put("sorce_doc_path", selectedFilePath);

                        RequestManager.addNewRequest(Session.GetInstance(), req);
                        FileHandler.createUploadThread(mContext, selectedFilePath, wakeLock);
                        mHandler.sendEmptyMessageDelayed(TIME_OUT, 1000);
                    }
                }
            });
        }
    }

    private static void createSpinners(View view) {
        //Display all the view of new request
        displayRequestItems(view);

        type = (Spinner) view.findViewById(R.id.spinner_type);
        type.setVisibility(View.VISIBLE);
        ArrayAdapter adapter1 = ArrayAdapter.createFromResource(mContext, R.array.type, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(adapter1);
        //type.setOnItemClickListener(mClickListener);

        //Select type of level
        level = (Spinner) view.findViewById(R.id.spinner_level);
        level.setVisibility(View.VISIBLE);
        ArrayAdapter adapter2 = ArrayAdapter.createFromResource(mContext, R.array.level, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        level.setAdapter(adapter2);

        payment = (Spinner) view.findViewById(R.id.spinner_pay);
        payment.setVisibility(View.VISIBLE);
        ArrayAdapter adapter3 = ArrayAdapter.createFromResource(mContext, R.array.pay, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        payment.setAdapter(adapter3);

        lang = (Spinner) view.findViewById(R.id.spinner_lang);
        lang.setVisibility(View.VISIBLE);
        ArrayAdapter adapter4 = ArrayAdapter.createFromResource(mContext, R.array.lang, android.R.layout.simple_spinner_item);
        adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lang.setAdapter(adapter4);
    }

    private static void displayRequestItems(View view) {
        TextView newText = (TextView) view.findViewById(R.id.new_request);
        TextView langTest = (TextView) view.findViewById(R.id.lang);
        TextView typeText = (TextView) view.findViewById(R.id.type);
        TextView levelText = (TextView) view.findViewById(R.id.level);
        TextView payText = (TextView) view.findViewById(R.id.pay);

        Button dateBt = (Button) view.findViewById(R.id.end_date);

        newText.setVisibility(View.VISIBLE);
        langTest.setVisibility(View.VISIBLE);
        typeText.setVisibility(View.VISIBLE);
        levelText.setVisibility(View.VISIBLE);
        payText.setVisibility(View.VISIBLE);
        dateBt.setVisibility(View.VISIBLE);
    }

    static public void showRequestList(Activity activity, View view) {
        final Activity ac = activity;
        Log.d(TAG, "Show Request List");

        // Show title
        TextView title1 = (TextView) view.findViewById(R.id.frag_exp_list_view_title1);
        TextView title2 = (TextView) view.findViewById(R.id.frag_exp_list_view_title2);
        title1.setVisibility(View.VISIBLE);
        title2.setVisibility(View.VISIBLE);

        // Prepare request item listview
        ExpandableListView mExpListView = (ExpandableListView) view.findViewById(R.id.request_list_view);
        ArrayList<Map<String, String>> mGroupList = new ArrayList<Map<String, String>>();
        ArrayList<ArrayList<Map<String, String>>> mChildList = new ArrayList<ArrayList<Map<String, String>>>();

        // Get my new request list
        JSONObject myprofile = ProfileManager.getMyInfo(Session.GetInstance());
        if (myprofile == null) {
            Log.e(TAG, "Profile info is null!");
            return;
        }
        String new_request_list = (String) myprofile.get("new_request");
        Log.d(TAG, new_request_list);

        // Get each request information
        StringTokenizer st = new StringTokenizer(new_request_list, ";"); // Parse new request list (ex. new_request_list = ";13;52;1;32")
        while (st.hasMoreTokens()) {
            String id_str = st.nextToken();
            int id = Integer.parseInt(id_str);
            Log.d(TAG, "id: " + id_str);

            JSONObject request = RequestManager.getRequestInfo(Session.GetInstance(), id);

            Map<String, String> curr = new HashMap<String, String>();
            curr.put("ID", id_str);
            curr.put("SUBJECT", (String) request.get("subject"));
            mGroupList.add(curr);

            ArrayList<Map<String, String>> children = new ArrayList<Map<String, String>>();
            Iterator<Object> itr = request.keySet().iterator();
            while (itr.hasNext()) {
                Object key = itr.next();
                String val = (String) request.get(key);
                if (val.equals("id") || val.equals("subject"))
                    continue;
                Map<String, String> child = new HashMap<String, String>();
                child.put("ITEM", (String) key);
                child.put("DATA", val);
                children.add(child);
            }
            mChildList.add(children);
        }

        mExpListView.setAdapter(new SimpleExpandableListAdapter(
                ac.getApplicationContext(),
                mGroupList,
                R.layout.request_list_row,
                new String[]{"ID", "SUBJECT"},
                new int[]{R.id.req_list_item_id, R.id.req_list_item_desc},
                mChildList,
                R.layout.detail_request_list_row,
                new String[]{"ITEM", "DATA"},
                new int[]{R.id.detail_req_list_item, R.id.detail_req_list_data}
        ));

        mExpListView.setVisibility(View.VISIBLE);
    }

    @TargetApi(Build.VERSION_CODES.DONUT)
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
            moveTaskToBack(true);
            finish();
//       `     android.os.Process.killProcess(android.os.Process.myPid());
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

        final int mode = ProfileManager.getUserMode(Session.GetInstance());
        if (mode == ProfileManager.USER_MODE.TRANSLATOR
                || mode == ProfileManager.USER_MODE.REVIEWER) {
            new Notifier(Notifier.Command.NEW_REQUEST, this.getApplicationContext());
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
        static public void showRequestItems(Activity activity, View view) {
            final Activity ac = activity;

            createSpinners(view);
            createDateSelector(view);
            createUploadItems(view, ac);
            //Listview
/*                ArrayAdapter<String> adapter2 = new ArrayAdapter<String> (mContext, android.R.layout.simple_list_item_1, paymentList);
                Spinner pay = (Spinner) view.findViewById(R.id.spinner_pay);
                pay.setAdapter(adapter2);
                pay.setOnItemClickListener(mClickListener);*/

        }

        private static void createDateSelector(View view) {
            Button endDate = (Button) view.findViewById(R.id.end_date);
            endDate.setVisibility(View.VISIBLE);
            if (endDate != null) {
                endDate.setVisibility(View.VISIBLE);
                endDate.setOnClickListener(new Button.OnClickListener() {
                    private DatePickerDialog.OnDateSetListener listener =
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    dueDate = year + "-" + monthOfYear + "-" + dayOfMonth;
                                    Toast.makeText(mContext, dueDate, Toast.LENGTH_SHORT).show();
                                }
                            };

                    @Override
                    public void onClick(View view) {
                        Calendar calendar = Calendar.getInstance();

                        DatePickerDialog dialog = new DatePickerDialog
                                (mContext, listener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
                        dialog.show();
                    }
                });
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.d(TAG, "onCreateView");

            View rootView = inflater.inflate(R.layout.fragment_client, container, false);
            int pageViewNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            switch (pageViewNumber) {
                case PAGE_NUM_NOTIFY:
                    showRequestList(getActivity(), rootView);
                    break;
                case PAGE_NUM_REQUEST:
                    showRequestItems(getActivity(), rootView);
                    break;
                case PAGE_NUM_PROFILE:
                default:
            }
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
