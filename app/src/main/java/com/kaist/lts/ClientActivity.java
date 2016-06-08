package com.kaist.lts;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
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
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class ClientActivity extends AppCompatActivity {
    static final String TAG = "[LTS][ClientActivity]";
    static final int PAGE_NUM_NOTIFY = 1;
    static final int PAGE_NUM_REQUEST = 2;
    static final int PAGE_NUM_PROFILE = 3;
    static final int PICK_FILE_REQUEST = 1;

    static public int TIME_OUT = 0;
    static public int UPLOADED = 1;
    static public Handler mHandler;
    static Context mContext;
    static FileHandler fh;
    static Spinner type;
    static Spinner level;
    static Spinner payment;
    static Spinner lang;
    static Spinner file;
    static String dueDate;
    static String selectedFilePath;
    static private PowerManager.WakeLock wakeLock;
    static private ProgressDialog dialog;
    static private HashMap<String, String> requesterDownloadableFilesMap
            = new HashMap<String, String>();
    static private HashMap<String, String> workerDownloadableFilesMap
            = new HashMap<String, String>();
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

    static private void createUploadItems(View view, final Activity ac) {
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
                        req.put("target_language", RequestManager.getLanuageNum((String)lang.getSelectedItem()));
                        req.put("doc_type", type.getSelectedItem());
                        req.put("level", level.getSelectedItem());
                        req.put("cost", payment.getSelectedItem());
                        req.put("due_date", dueDate);
                        String[] truncatedFilePath = selectedFilePath.split("/");
                        String fileName = truncatedFilePath[truncatedFilePath.length - 1];

                        //Get user mode from server
                        if (ProfileManager.user_mode == -1) {
                            ProfileManager.getUserMode(Session.GetInstance());
                        }

                        if (ProfileManager.user_mode == ProfileManager.USER_MODE.REQUESTER) {
                            fileName = "1_" + fileName;
                            req.put("source_doc_path", fileName);
                        } else if (ProfileManager.user_mode == ProfileManager.USER_MODE.TRANSLATOR) {
                            fileName = "2_" + fileName;
                            req.put("translated_doc_path", fileName);
                        } else if (ProfileManager.user_mode == ProfileManager.USER_MODE.REVIEWER) {
                            fileName = "3_" + fileName;
                            req.put("reviewed_doc_path", fileName);
                        } else {
                            req.put("source_doc_path", fileName);
                        }

                        Log.d(TAG, "File rename: " + fileName);

                        int request_id = RequestManager.addNewRequest(Session.GetInstance(), req);
                        if (request_id <= 0) {
                            Log.e(TAG, "Fail to Add new request, id:"+Integer.toString(request_id));
                            return;
                        }

                        FileHandler.createUploadThread(mContext, selectedFilePath, wakeLock, fileName);
                        mHandler.sendEmptyMessageDelayed(TIME_OUT, 60 * 1000); //1min

                        new Notifier(Notifier.Command.LIST_OF_CANDIDATES, request_id, mContext); // To get notification of candidate reviewers.
                    }
                }
            });
        }
    }

    private static void createSpinners(View view) {

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

        if (ProfileManager.user_mode == -1) {
            ProfileManager.getUserMode(Session.GetInstance());
        }

        HashMap<String, String> downloadFilesMap = getDownloadableFilesMap(ProfileManager.user_mode);

        if (downloadFilesMap != null) {
            Log.d(TAG, "Map size: " + downloadFilesMap.size());
            file = (Spinner) view.findViewById(R.id.spinner_file);
            file.setVisibility(View.VISIBLE);

            //ArrayAdapter<String> adapter5
            //        = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item);
            ArrayAdapter<HashMap<String, String>> adapter5
                    = new ArrayAdapter<HashMap<String, String>>(view.getContext(), android.R.layout.simple_spinner_item);

            adapter5.add(downloadFilesMap);
            adapter5.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            file.setAdapter(adapter5);
        }
    }

    private static void displayRequestItems(View view) {
        TextView newText = (TextView) view.findViewById(R.id.new_request);
        TextView langTest = (TextView) view.findViewById(R.id.lang);
        TextView typeText = (TextView) view.findViewById(R.id.type);
        TextView levelText = (TextView) view.findViewById(R.id.level);
        TextView payText = (TextView) view.findViewById(R.id.pay);
        TextView fileText = (TextView) view.findViewById(R.id.files);

        Button dateBt = (Button) view.findViewById(R.id.due_date);

        newText.setVisibility(View.VISIBLE);
        langTest.setVisibility(View.VISIBLE);
        typeText.setVisibility(View.VISIBLE);
        levelText.setVisibility(View.VISIBLE);
        payText.setVisibility(View.VISIBLE);
        fileText.setVisibility(View.VISIBLE);
        dateBt.setVisibility(View.VISIBLE);
    }

    static private void showRequestList(Activity activity, View view, int user_mode) {
        final Activity ac = activity;
        Log.d(TAG, "Show Request List");

        // Show title
        TextView title1 = (TextView) view.findViewById(R.id.frag_exp_list_view_title1);
        TextView title2 = (TextView) view.findViewById(R.id.frag_exp_list_view_title2);
        title1.setVisibility(View.VISIBLE);
        title2.setVisibility(View.VISIBLE);

        // Get my profiles
        JSONObject myprofile = null;
        myprofile = ProfileManager.getMyInfo(Session.GetInstance());
        if (myprofile == null) {
            Log.e(TAG, "No response from ProfileManager.getMyInfo");
            return;
        }

        // Get my all requests list (If the user is Translator or Reviewr, Include work-done, working, and not-applied requests)
        String target_column_name;
        if (user_mode == ProfileManager.USER_MODE.REQUESTER)
            target_column_name = "worklist";
        else
            target_column_name = "new_request";
        String work_list = (String)myprofile.get(target_column_name);
        Log.d(TAG, target_column_name + ": " + work_list);

        // Get not-applied requests (
        Set<String> applied_requests_set = null;
        if (user_mode != ProfileManager.USER_MODE.REQUESTER) {
            String applied_requests_list=(String) myprofile.get("_applied_request");
            applied_requests_set = new HashSet<String>();
            Collections.addAll(applied_requests_set, applied_requests_list.split(";"));
            Log.d(TAG, "_applied_request: " + applied_requests_list);
        }

        // Get each request information and  Fill the Group/Child data of Expandable ListView
        ArrayList<Map<String, String>> mGroupList = new ArrayList<Map<String, String>>();
        ArrayList<ArrayList<Map<String, String>>> mChildList = new ArrayList<ArrayList<Map<String, String>>>();
        StringTokenizer st = new StringTokenizer(work_list, ";"); // Parse new request list (ex. work_list = ";13;52;1;32")

        while (st.hasMoreTokens()) {
            String id_str = st.nextToken();
            int id = Integer.parseInt(id_str);
            Log.d(TAG, "id: " + id_str);

            JSONObject request = RequestManager.getRequestInfo(Session.GetInstance(), id);

            Map<String, String> curr = new HashMap<String, String>();
            curr.put("ID", id_str);
            curr.put("SUBJECT", (String) request.get("subject"));
            String file = (String) request.get("final_doc_path");

            setDownloadableMap(request, file);
            if (applied_requests_set != null) {
                if (applied_requests_set.contains(id_str))
                    curr.put("IS_APPLIED", "TRUE");
                else
                    curr.put("IS_APPLIED", "FALSE");
            }
            mGroupList.add(curr);

            ArrayList<Map<String, String>> children = new ArrayList<Map<String, String>>();
            Iterator<Object> itr = request.keySet().iterator();
            while (itr.hasNext()) {
                Object key = itr.next();
                String allowedTerm = ItemFilter.GetAllowedTermForRequestColumn((String)key, user_mode);
                if (allowedTerm == null) // Filtering
                    continue;
                String val = (String) request.get(key); // Column
                Map<String, String> child = new HashMap<String, String>();
                child.put("ITEM", allowedTerm);
                child.put("KEY", (String)key);
                val = val.replace(';',' ');
                if (((String) key).contains("language"))
                    val = RequestManager.getLanuageString(Integer.parseInt(val));
                child.put("DATA", val);
                children.add(child);
            }
            mChildList.add(children);
        }

        final ExpandableListView mExpListView = (ExpandableListView) view.findViewById(R.id.request_list_view);
        mExpListView.setAdapter(new customExpandableListAdapter(
                ac.getApplicationContext(),
                mGroupList,
                R.layout.request_list_row,
                new String[]{"ID", "SUBJECT"},
                new int[]{R.id.req_list_item_id, R.id.req_list_item_desc},
                mChildList,
                R.layout.detail_request_list_row,
                new String[]{"ITEM", "DATA"},
                new int[]{R.id.detail_req_list_item, R.id.detail_req_list_data},
                mExpListView, user_mode)
        );
        Log.d(TAG, "SetAdaptor");
        mExpListView.setVisibility(View.VISIBLE);
    }

    static public HashMap<String, String> getDownloadableFilesMap(int mode) {
        // Get my profiles
        JSONObject myprofile = null;
        myprofile = ProfileManager.getMyInfo(Session.GetInstance());
        if (myprofile == null) {
            Log.e(TAG, "No response from ProfileManager.getMyInfo");
            return null;
        }

        // Get my all requests list (Include work-done, working, and not-applied requests)
        String all_worklist = (String) myprofile.get("worklist");
        Log.d(TAG, "all_worklist: " + all_worklist);

        // Get each request information and  Fill the Group/Child data of Expandable ListView
        ArrayList<Map<String, String>> mGroupList = new ArrayList<Map<String, String>>();
        ArrayList<ArrayList<Map<String, String>>> mChildList = new ArrayList<ArrayList<Map<String, String>>>();
        StringTokenizer st = new StringTokenizer(all_worklist, ";"); // Parse new request list (ex. all_worklist = ";13;52;1;32")

        while (st.hasMoreTokens()) {
            String id_str = st.nextToken();
            int id = Integer.parseInt(id_str);
            Log.d(TAG, "id: " + id_str);

            JSONObject request = RequestManager.getRequestInfo(Session.GetInstance(), id);

            String file;
            if (mode == ProfileManager.USER_MODE.REQUESTER) {
                file = (String) request.get("final_doc_path");
                setDownloadableMap(request, file);
            } else {
                file = (String) request.get("source_doc_path");
                setDownloadableMap(request, file);

                file = (String) request.get("reviewed_doc_path");
                setDownloadableMap(request, file);
            }
            Log.d(TAG, "Add file to map: " + file);
        }

        if (requesterDownloadableFilesMap != null) {
            return requesterDownloadableFilesMap;
        } else {
            return null;
        }
/*        if (requesterDownloadableFilesMap != null
                && mode == ProfileManager.USER_MODE.REQUESTER) {
            Log.d(TAG, "using:  requesterDownloadableFilesMap");
            return requesterDownloadableFilesMap;
        } else if (workerDownloadableFilesMap != null
                && (mode == ProfileManager.USER_MODE.REVIEWER
                || mode == ProfileManager.USER_MODE.TRANSLATOR)) {
            Log.d(TAG, "using: workerDownloadableFilesMap");
            return workerDownloadableFilesMap;
        } else {
            Log.e(TAG, "File Map is NULL!");
            return null;
        }*/
    }

    private static void setDownloadableMap(JSONObject request, String file) {
        if (file != null && !file.isEmpty()) {
            requesterDownloadableFilesMap.put("FILE", (String) request.get("final_doc_path"));
        }
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
                if (msg.what == TIME_OUT || msg.what == UPLOADED) {
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

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Log.d(TAG, "onKeyUp");

            AlertDialog.Builder ad = new AlertDialog.Builder(mContext);
            ad.setTitle("Options")
                    .setMessage("Please select options")
                    .setCancelable(true)
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "Left: Exit");
                            moveTaskToBack(true);
                            finish();
                        }
                    })
                    .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "Middle: Cancel");
                            dialog.cancel();
                        }
                    })
                    .setNegativeButton("Logout", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "Right: Logout");
                            MainActivity.callFacebookLogout();
                            moveTaskToBack(true);
                            finish();
/*                          Intent intentHome = new Intent(mContext, MainActivity.class);
                            intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intentHome.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intentHome);
                            finish();*/
                        }
                    });
            AlertDialog dialog = ad.create();
            dialog.show();
        }

        return true;//super.onKeyUp(keyCode, event);
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
                Log.d(TAG, "Selected file:" + selectedFilePath);

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

    public static class customExpandableListAdapter extends SimpleExpandableListAdapter {
        final List<? extends List<? extends Map<String, ?>>> mChildData;
        final private ExpandableListView mExpListView;
        final private int mUserMode;


        public customExpandableListAdapter(Context context,
                                    List<? extends Map<String, ?>> groupData, int groupLayout,
                                    String[] groupFrom, int[] groupTo,
                                    List<? extends List<? extends Map<String, ?>>> childData,
                                    int childLayout, String[] childFrom, int[] childTo,
                                    ExpandableListView expListView, int userMode) {
            super(context, groupData, groupLayout, groupLayout, groupFrom, groupTo, childData,
                    childLayout, childLayout, childFrom, childTo);
            mChildData = childData;
            mExpListView = expListView;
            mUserMode = userMode;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                                 ViewGroup parent) {
            View view = super.getGroupView(groupPosition, isExpanded, convertView, parent);
            if (mUserMode == ProfileManager.USER_MODE.REQUESTER) {
                TextView tview = (TextView)view.findViewById(R.id.req_list_item_desc);
                tview.setLayoutParams(new LinearLayout.LayoutParams
                        (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                return view;
            }

            // Show Apply Button If it's new requests (Not applied yet)
            // Only for Translator and Reviewer
            Map<String, String> groupdata = (Map<String, String>) super.getGroup(groupPosition);
            final Button apply_btn = (Button) view.findViewById(R.id.request_apply_btn);
            if (groupdata.get("IS_APPLIED").equals("TRUE")) {
                Log.d(TAG, "IS_APPLIED = TRUE");
                disableButton(apply_btn, "Applied");
            } else {
                enableButton(apply_btn, "Apply");
                apply_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "btn onClick");
                        int request_id = Integer.parseInt((String) view.getTag(R.id.request_apply_btn));
                        if (!RequestManager.bid(Session.GetInstance(), request_id))
                            Log.d(TAG, "Failed to BID, req_id:" + Integer.toString(request_id));
                        disableButton(apply_btn, "Applied");
                        ExpandableListAdapter ad = mExpListView.getExpandableListAdapter();
                        Map<String, String> groupdata = (Map<String, String>) ad.getGroup((int) view.getTag(R.id.request_apply_btn + 1));
                        groupdata.put("IS_APPLIED", "TRUE");
                        new Notifier(Notifier.Command.RESULT_OF_BID, request_id, mContext);
                    }
                });
            }
            /*Log.d(TAG, "getGroupView  pos"+Integer.toString(groupPosition)+
                    ", APPLIED:"+groupdata.get("IS_APPLIED")+", ID:"+groupdata.get("ID"));*/
            apply_btn.setVisibility(View.VISIBLE);
            apply_btn.setTag(R.id.request_apply_btn, groupdata.get("ID"));
            apply_btn.setTag(R.id.request_apply_btn + 1, groupPosition);
            return view;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                                 View convertView, ViewGroup parent) {
            View view = super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);
            if (mUserMode == ProfileManager.USER_MODE.REVIEWER)
                return view;

            // Show Select Button If it's candidates list
            // Only for Requester and Translator
            Map<String, String> childdata = (Map<String, String>) super.getChild(groupPosition, childPosition);
            final Button select_btn = (Button) view.findViewById(R.id.select_worker_btn);
            String target, target2;
            if (mUserMode == ProfileManager.USER_MODE.REQUESTER) {
                target = "translator_candidate_list";
                target2 = "translator_id";
            }else {// if current user is translator
                target = "reviewer_candidate_list";
                target2 = "reviewer_id";
            }

            if (false == childdata.get("KEY").equals(target) || childdata.get("DATA").isEmpty()) {
                select_btn.setVisibility(View.INVISIBLE);
            } else {
                // If this group has worker_id , disable Button;
                List<Map<String, String>> children = (List<Map<String, String>>)mChildData.get(groupPosition);
                Iterator<Map<String, String>> itr = children.iterator();
                boolean isSelected = false;
                while (itr.hasNext()) {
                    Map<String, String> key = itr.next();
                    if (key.get("KEY").equals(target2)) {
                        if (key.get("DATA").isEmpty() == false)
                            isSelected = true;
                        break;
                    }
                }

                if (isSelected) {
                    disableButton(select_btn, "Selected");
                } else {
                    enableButton(select_btn, "Select Worker");
                    ExpandableListAdapter ad = mExpListView.getExpandableListAdapter();
                    Map<String, String> groupdata = (Map<String, String>) ad.getGroup(groupPosition);
                    select_btn.setTag(groupdata.get("ID")); // Request id
                    select_btn.setTag(R.id.select_worker_btn, childdata.get("DATA")); // Candidates list
                    select_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG, "select btn onClick, Show Diaglog to select worker");
                            // Show candidates list on Dialog  , String candidates = view.getTag();
                            Intent intent = new Intent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("candidates_list", (String)view.getTag(R.id.select_worker_btn));
                            intent.putExtra("request_id", (String)view.getTag());
                            intent.setClassName(AccessManager.LTS_PACKAGE_NAME, AccessManager.SELWKR_CLASS_NAME);
                            mContext.startActivity(intent);
                        }
                    });
                    select_btn.setVisibility(View.VISIBLE);
                }
            }
            return view;
        }

        private void disableButton(final Button btn, String text) {
            btn.setText(text);
            btn.setOnClickListener(null);
            btn.setClickable(false);
            btn.setBackgroundColor(0xFF9AF0E5);
        }

        private void enableButton(final Button btn, String text) {
            btn.setText(text);
            btn.setBackgroundColor(0xFF54C7B8);
            btn.setClickable(true);
        }
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
            //Display all the view of new request
            displayRequestItems(view);

            createSpinners(view);
            createDateSelector(view);
            createUploadItems(view, ac);
            createDownloadItems(view);
            //Listview
/*                ArrayAdapter<String> adapter2 = new ArrayAdapter<String> (mContext, android.R.layout.simple_list_item_1, paymentList);
                Spinner pay = (Spinner) view.findViewById(R.id.spinner_pay);
                pay.setAdapter(adapter2);
                pay.setOnItemClickListener(mClickListener);*/

        }

        private static void createDownloadItems(View v) {
            Log.d(TAG, "Show download button");
            Button dl = (Button) v.findViewById(R.id.down_button);

            if (dl != null) {
                dl.setVisibility(View.VISIBLE);
                dl.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HashMap<String, String> map = (HashMap<String, String>) file.getSelectedItem();
                        String file = map.get("FILE");
                        if (file == null) {
                            Toast.makeText(mContext, "Please select the document", Toast.LENGTH_SHORT);
                            return;
                        }

                        FileHandler.downloadFile(mContext, file);
                    }
                });
            }
        }

        private static void createDateSelector(View v) {
            Button endDate = (Button) v.findViewById(R.id.due_date);
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
            int user_mode = ProfileManager.getUserMode(Session.GetInstance());
            switch (pageViewNumber) {
                case PAGE_NUM_NOTIFY:
                    showRequestList(getActivity(), rootView, user_mode);
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
