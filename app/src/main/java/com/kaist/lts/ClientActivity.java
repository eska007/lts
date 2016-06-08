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
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
    static final int PICK_FILE_REQUEST1 = 1;
    static final int PICK_FILE_REQUEST2 = 2;

    static public int TIME_OUT = 0;
    static public int UPLOADED = 1;
    static public int ERROR = -1;
    static public Handler mHandler;
    static Context mContext;
    static Activity mActivity;
    static FileHandler fh;
    static private Spinner type;
    static private Spinner level;
    static private Spinner payment;
    static private Spinner lang;
    static private Spinner file;
    static private Spinner subject;
    static private String dueDate;
    static private String selectedFilePath;
    static private TextView subjectEdit;
    static private PowerManager.WakeLock wakeLock;
    static private ProgressDialog dialog;
    static private Set<String> downloadableFilesSet;
    static private Set<String> subjectListSet;
    static private Map<String, String> workListMap;

    static private TextView langText;
    static private TextView typeText;
    static private TextView levelText;
    static private TextView payText;

    //static private LinearLayout dateBt;
    static private Button dateBt;
    static private Button getBt;
    static private Button updateBt;

    static private LinearLayout datelay;
    static private LinearLayout uploadlay;
    static private LinearLayout attachlay;
    static private ImageView attachIcon;
    static private JSONObject myWorkInfo;
    static private JSONObject mMyprofile;
    static private String upload_file_request_id;
    static private String upload_file_column;

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

    static private void showFileChooser(Activity activity, int requestCode, String request_id, String column) {
        Intent intent = new Intent();
        //sets the select file to all types of files
        intent.setType("file/*");
        //allows to select data and return it
        intent.setAction(Intent.ACTION_GET_CONTENT);
        if (request_id != null)
            upload_file_request_id = new String(request_id); // intent.putExtra("request_id", request_id);
        if (column != null)
            upload_file_column = new String(column); // intent.putExtra("column", column);
        Log.d(TAG, "request_id: " + upload_file_request_id + ", column:" + upload_file_column);
        //starts new activity to select file and return data
        activity.startActivityForResult(Intent.createChooser(intent, "Choose File to Upload.."), requestCode);
    }

    static private void createUploadItems(View view, final Activity ac) {
        Log.d(TAG, "Show upload items");

        if (ProfileManager.user_mode == -1) {
            ProfileManager.getUserMode(Session.GetInstance());
        }

        if (attachlay != null) {
            Log.d(TAG, "Show attach icon");
            attachlay.setVisibility(View.VISIBLE);
            attachIcon.setVisibility(View.VISIBLE);
            //ImageView attachIcon = (ImageView) view.findViewById(R.id.attach_icon);
            attachIcon.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fh = new FileHandler();

                    showFileChooser(ac, PICK_FILE_REQUEST1, null, null);
                }
            });

        }

        if (uploadlay != null) {
            Log.d(TAG, "Show upload button");

            uploadlay.setVisibility(View.VISIBLE);
            updateBt.setVisibility(View.VISIBLE);
            updateBt.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadFile();
                }
            });
        }
    }


    private static void uploadFileDirect(String request_id, String column, String reName) {
        Log.d(TAG, "Display progress bar");
        if (fh == null) {
            Log.e(TAG, "fh == null");
        }

        //Generate json format
        JSONObject req = new JSONObject();
        req.put("id", Integer.parseInt(request_id));
        req.put("column", column);

        String[] truncatedFilePath = selectedFilePath.split("/");
        String fileName = truncatedFilePath[truncatedFilePath.length - 1];
        if (reName == null) {
            dialog = ProgressDialog.show(mContext, "", "Uploading File...", true);
            req.put("path", fileName);
        } else {
            req.put("path", reName);
            fileName = reName;
        }

        Log.d(TAG, request_id + ", " + column + ", File rename: " + fileName);
        RequestManager.uploadData(Session.GetInstance(), req);
        FileHandler.createUploadThread(mContext, null, selectedFilePath, wakeLock, fileName);
        selectedFilePath = null;
        mHandler.sendEmptyMessageDelayed(TIME_OUT, 60 * 1000); //1 min
    }

    private static void uploadFile() {
        Log.d(TAG, "Display progress bar");
        if (fh == null) {
            Log.e(TAG, "fh == null");
        }

        dialog = ProgressDialog.show(mContext, "", "Uploading File...", true);

        //Generate json format
        JSONObject req = new JSONObject();
        String[] truncatedFilePath = selectedFilePath.split("/");
        String fileName = truncatedFilePath[truncatedFilePath.length - 1];
        if (ProfileManager.user_mode == -1) {
            ProfileManager.getUserMode(Session.GetInstance());
        }

        if (ProfileManager.user_mode == ProfileManager.USER_MODE.REQUESTER) {

            //req.put("id", Login.id);
            req.put("subject", subjectEdit.getText().toString());
            req.put("target_language", RequestManager.getLanuageNum((String) lang.getSelectedItem()));
            req.put("doc_type", type.getSelectedItem());
            req.put("level", level.getSelectedItem());
            req.put("cost", payment.getSelectedItem());
            req.put("due_date", dueDate);

            //Get user mode from server
            fileName = "1_" + fileName;
            req.put("source_doc_path", fileName);
        } else if (ProfileManager.user_mode == ProfileManager.USER_MODE.TRANSLATOR) {
            fileName = "2_" + fileName;
            req.put("translated_doc_path", fileName);
            String list = (String) subject.getSelectedItem();
            Log.d(TAG, "TRANSLATOR - Selected ID: " + list);
            uploadFileDirect(workListMap.get(list), "translated_doc_path", fileName);
            return;
        } else if (ProfileManager.user_mode == ProfileManager.USER_MODE.REVIEWER) {
            fileName = "3_" + fileName;
            req.put("reviewed_doc_path", fileName);
            String list = (String) subject.getSelectedItem();
            Log.d(TAG, "REVIEWER - Selected ID: " + list);
            uploadFileDirect(workListMap.get(list), "reviewed_doc_path", fileName);
            return;
        } else {
            req.put("source_doc_path", fileName);
        }

        Log.d(TAG, "File rename: " + fileName);

        FileHandler.createUploadThread(mContext, req, selectedFilePath, wakeLock, fileName);
        selectedFilePath = null;
        mHandler.sendEmptyMessageDelayed(TIME_OUT, 60 * 1000); //1 min
    }

    static public void updateNotifyMsg(JSONObject req) {
        int request_id = RequestManager.addNewRequest(Session.GetInstance(), req);
        if (request_id <= 0) {
            Log.e(TAG, "Fail to Add new request, id:" + Integer.toString(request_id));
            return;
        }

        new Notifier(Notifier.Command.LIST_OF_CANDIDATES, request_id, mContext); // To get notification of candidate reviewers.
    }

    private static void createSpinners(View view) {

        type = (Spinner) view.findViewById(R.id.spinner_type);

        ArrayAdapter adapter1 = ArrayAdapter.createFromResource(mContext, R.array.type, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(adapter1);
        //type.setOnItemClickListener(mClickListener);

        //Select type of level
        level = (Spinner) view.findViewById(R.id.spinner_level);

        ArrayAdapter adapter2 = ArrayAdapter.createFromResource(mContext, R.array.level, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        level.setAdapter(adapter2);

        payment = (Spinner) view.findViewById(R.id.spinner_pay);

        ArrayAdapter adapter3 = ArrayAdapter.createFromResource(mContext, R.array.pay, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        payment.setAdapter(adapter3);

        lang = (Spinner) view.findViewById(R.id.spinner_lang);

        ArrayAdapter adapter4 = ArrayAdapter.createFromResource(mContext, R.array.lang, android.R.layout.simple_spinner_item);
        adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lang.setAdapter(adapter4);

        createDownloadFileSpinner(view);
    }

    private static void createDownloadFileSpinner(View view) {
        if (ProfileManager.user_mode == -1) {
            ProfileManager.getUserMode(Session.GetInstance());
        }

        Set<String> downloadFilesSet = getDownloadableFilesSet(ProfileManager.user_mode);

        if (downloadFilesSet != null) {
            int fileCount = downloadFilesSet.size();
            Log.d(TAG, "size: " + fileCount);

            if (fileCount > 0) {
                PlaceholderFragment.createDownloadItems(view);
                TextView fileText = (TextView) view.findViewById(R.id.files);
                fileText.setVisibility(View.VISIBLE);

                file = (Spinner) view.findViewById(R.id.spinner_file);
                file.setVisibility(View.VISIBLE);

                ArrayAdapter<String> adapter
                        = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item);

                Iterator<String> iterator = downloadFilesSet.iterator();
                while (iterator.hasNext()) {
                    String fileName = iterator.next();
                    adapter.add(fileName);
                    Log.d(TAG, "Show downloadable file: " + fileName);
                }

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                file.setAdapter(adapter);
            }
        }
    }

    private static void createSubjectSpinner(View v) {
        if (ProfileManager.user_mode == -1) {
            ProfileManager.getUserMode(Session.GetInstance());
        }

        Set<String> SubjectSet = getSubjectListSet();

        if (SubjectSet != null) {
            int subjectCount = SubjectSet.size();
            Log.d(TAG, "subject count: " + subjectCount);

            if (subjectCount > 0) {
                Log.d(TAG, "Show subject lists");

                Button bt = (Button) v.findViewById(R.id.get_button);

                if (bt != null) {
                    bt.setVisibility(View.VISIBLE);
                    bt.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String lists = (String) subject.getSelectedItem();

                            if (lists == null || lists.isEmpty()) {
                                Toast.makeText(mContext, "Please select the subject", Toast.LENGTH_SHORT);
                                return;
                            }
                        }
                    });
                }

                subject = (Spinner) v.findViewById(R.id.spinner_subject);
                subject.setVisibility(View.VISIBLE);

                ArrayAdapter<String> adapter
                        = new ArrayAdapter<String>(v.getContext(), android.R.layout.simple_spinner_item);

                Iterator<String> iterator = SubjectSet.iterator();
                while (iterator.hasNext()) {
                    String list = iterator.next();
                    adapter.add(list);
                    Log.d(TAG, "Show subject lists: " + list);
                }

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                subject.setAdapter(adapter);
            }
        }
    }

    private static void displayRequestItems(View view, final Activity ac) {
        TextView newText = (TextView) view.findViewById(R.id.new_request);
        TextView subjectText = (TextView) view.findViewById(R.id.subject);
        newText.setVisibility(View.VISIBLE);
        subjectText.setVisibility(View.VISIBLE);

        langText = (TextView) view.findViewById(R.id.lang);
        typeText = (TextView) view.findViewById(R.id.type);
        levelText = (TextView) view.findViewById(R.id.level);
        payText = (TextView) view.findViewById(R.id.pay);

        datelay = (LinearLayout) view.findViewById(R.id.due_date_layout);
        attachlay = (LinearLayout) view.findViewById(R.id.attach_layout);
        uploadlay = (LinearLayout) view.findViewById(R.id.upload_layout);

        dateBt = (Button) view.findViewById(R.id.due_date);
        getBt = (Button) view.findViewById(R.id.get_button);
        updateBt = (Button) view.findViewById(R.id.upload_button);

        attachIcon = (ImageView) view.findViewById(R.id.attach_icon);

        if (ProfileManager.user_mode == -1) {
            ProfileManager.getUserMode(Session.GetInstance());
        }

        switch (ProfileManager.user_mode) {
            case ProfileManager.USER_MODE.REQUESTER:
                subjectEdit = (EditText) view.findViewById(R.id.subject_desc);
                subjectEdit.setVisibility(View.VISIBLE);

                langText.setVisibility(View.VISIBLE);
                typeText.setVisibility(View.VISIBLE);
                levelText.setVisibility(View.VISIBLE);
                payText.setVisibility(View.VISIBLE);
                datelay.setVisibility(View.VISIBLE);
                break;

            case ProfileManager.USER_MODE.TRANSLATOR:
            case ProfileManager.USER_MODE.REVIEWER:
                createSubjectSpinner(view);
                //dateBt.setVisibility(View.GONE);
                datelay.setVisibility(View.GONE);
                getBt.setVisibility(View.VISIBLE);
                getBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //Get ID info from hashMap
                        String str = (String) subject.getSelectedItem();
                        Log.d(TAG, "Selected Subject: " + str);

                        String ids = workListMap.get(str);
                        Log.d(TAG, "Selected ID: " + ids);

                        int id = Integer.parseInt(ids);
                        getSubjectSpecificInfo(id, view, ac);
                    }
                });
                break;
            default:
                break;
        }
    }

    static private void getSubjectSpecificInfo(int id, final View v, final Activity ac) {
        // Get my profiles
        getMyWorkListInfo();

        if (myWorkInfo == null) {
            Log.e(TAG, "No response from ProfileManager.getMyInfo");
            return;
        }
        //getBt.setVisibility(View.GONE);

        JSONObject obj = RequestManager.getRequestInfo(Session.GetInstance(), id);

        if (obj != null) {
            langText.setVisibility(View.VISIBLE);
            typeText.setVisibility(View.VISIBLE);
            levelText.setVisibility(View.VISIBLE);
            payText.setVisibility(View.VISIBLE);
            datelay.setVisibility(View.VISIBLE);
            //dateBt.setVisibility(View.VISIBLE);
            String lang = (String) obj.get("target_language");
            if (lang.equals("0")) {
                lang = "Korean";
            } else if (lang.equals("1")) {
                lang = "English";
            } else if (lang.equals("2")) {
                lang = "Chinese";
            } else if (lang.equals("3")) {
                lang = "Japanese";
            }
            langText.setText("Language : " + lang);
            typeText.setText("Type : " + obj.get("doc_type"));
            levelText.setText("Level : " + obj.get("level"));
            payText.setText("Payment : " + obj.get("cost") + " per page");
            dateBt.setText("Due to : " + obj.get("due_date"));

            createUploadItems(v, ac);
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    static private void showWorkList(Activity activity, View view, int user_mode) {
        final Activity ac = activity;
        Log.d(TAG, "Show Request List");

        // Show title
        TextView title = (TextView) view.findViewById(R.id.work_list_text);
        TextView title1 = (TextView) view.findViewById(R.id.frag_exp_list_view_title1);
        TextView title2 = (TextView) view.findViewById(R.id.frag_exp_list_view_title2);
        title.setVisibility(View.VISIBLE);
        title1.setVisibility(View.VISIBLE);
        title2.setVisibility(View.VISIBLE);

        // Get my profiles
        getMyWorkListInfo();

        if (myWorkInfo == null) {
            Log.e(TAG, "No response from ProfileManager.getMyInfo");
            return;
        }

        // Get my all requests list (If the user is Translator or Reviewr, Include work-done, working, and not-applied requests)
        String target_column_name;
        if (user_mode == ProfileManager.USER_MODE.REQUESTER)
            target_column_name = "worklist";
        else
            target_column_name = "new_request";
        String work_list = (String) myWorkInfo.get(target_column_name);
        Log.d(TAG, target_column_name + ": " + work_list);

        Set<String> applied_requests_set = null;
        if (user_mode != ProfileManager.USER_MODE.REQUESTER) {
            String applied_requests_list = (String) myWorkInfo.get("_applied_request");
            applied_requests_set = new HashSet<String>();
            Collections.addAll(applied_requests_set, applied_requests_list.split(";"));
            //Log.d(TAG, "_applied_request: " + applied_requests_list);
        }

        Set<String> worklist_set = null;
        if (user_mode != ProfileManager.USER_MODE.REQUESTER) {
            String worklist = (String) myWorkInfo.get("worklist");
            worklist_set = new HashSet<String>();
            Collections.addAll(worklist_set, worklist.split(";"));
            //Log.d(TAG, "worklist: " + worklist);
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
            if (request == null)
                continue;

            Map<String, String> curr = new HashMap<String, String>();
            curr.put("ID", id_str);
            curr.put("SUBJECT", (String) request.get("subject"));

            setWorkListMap(id_str, request);

            String file = (String) request.get("final_doc_path");
            if (file.isEmpty() == false)
                curr.put("IS_FINISHED", "TRUE");

            //setDownloadableMap(request, file);
            if (applied_requests_set != null) {
                if (applied_requests_set.contains(id_str)) {
                    curr.put("IS_APPLIED", "TRUE");
                } else {
                    curr.put("IS_APPLIED", "FALSE");
                }
            }

            if (worklist_set != null && worklist_set.contains(id_str))
                curr.put("IS_EMPLOYED", "TRUE");
            else
                curr.put("IS_EMPLOYED", "FALSE");

            mGroupList.add(curr);

            ArrayList<Map<String, String>> children = new ArrayList<Map<String, String>>();
            Iterator<Object> itr = request.keySet().iterator();
            while (itr.hasNext()) {
                Object key = itr.next();
                String allowedTerm = ItemFilter.GetAllowedTermForRequestColumn((String) key, user_mode);
                if (allowedTerm == null) // Filtering
                    continue;
                String val = (String) request.get(key); // Column
                Map<String, String> child = new HashMap<String, String>();
                child.put("ITEM", allowedTerm);
                child.put("KEY", (String) key);
                val = val.replace(';', ' ');
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

    private static void setWorkListMap(String id, JSONObject obj) {
        if (obj == null)
            return;

        if (workListMap == null) {
            workListMap = new HashMap<String, String>();
        }

        String str = (String) obj.get("subject");

        if (str != null && !str.isEmpty()) {
            Log.d(TAG, "Set subject to map: " + str);
            workListMap.put(str, id);
        }
    }

    private static void getMyWorkListInfo() {
        if (myWorkInfo == null) {
            myWorkInfo = ProfileManager.getMyInfo(Session.GetInstance());
        }
    }

    static public Set<String> getDownloadableFilesSet(int mode) {
        // Get my profiles
        getMyWorkListInfo();

        myWorkInfo = ProfileManager.getMyInfo(Session.GetInstance());
        if (myWorkInfo == null) {
            Log.e(TAG, "No response from ProfileManager.getMyInfo");
            return null;
        }

        // Get my all requests list (Include work-done, working, and not-applied requests)
        String all_worklist = (String) myWorkInfo.get("worklist");
        Log.d(TAG, "all_worklist: " + all_worklist);

        // Get each request information and  Fill the Group/Child data of Expandable ListView
        StringTokenizer st = new StringTokenizer(all_worklist, ";"); // Parse new request list (ex. all_worklist = ";13;52;1;32")

        downloadableFilesSet = new HashSet<String>();
        while (st.hasMoreTokens()) {
            String id_str = st.nextToken();
            int id = Integer.parseInt(id_str);
            Log.d(TAG, "id: " + id_str);

            JSONObject request = RequestManager.getRequestInfo(Session.GetInstance(), id);

            String file;
            if (mode == ProfileManager.USER_MODE.REQUESTER) {
                setSpinnerSet(downloadableFilesSet, (String) request.get("final_doc_path"));
            } else if (mode == ProfileManager.USER_MODE.TRANSLATOR) {
                setSpinnerSet(downloadableFilesSet, (String) request.get("source_doc_path"));
                setSpinnerSet(downloadableFilesSet, (String) request.get("reviewed_doc_path"));
            } else if (mode == ProfileManager.USER_MODE.REVIEWER) {
                setSpinnerSet(downloadableFilesSet, (String) request.get("translated_doc_path"));
            }
        }

        if (downloadableFilesSet != null) {
            return downloadableFilesSet;
        } else {
            return null;
        }
    }

    static public Set<String> getSubjectListSet() {
        // Get my profiles
        getMyWorkListInfo();

        myWorkInfo = ProfileManager.getMyInfo(Session.GetInstance());
        if (myWorkInfo == null) {
            Log.e(TAG, "No response from ProfileManager.getMyInfo");
            return null;
        }

        // Get my all requests list (Include work-done, working, and not-applied requests)
        String all_worklist = (String) myWorkInfo.get("worklist");
        Log.d(TAG, "all_worklist: " + all_worklist);

        // Get each request information and  Fill the Group/Child data of Expandable ListView
        StringTokenizer st = new StringTokenizer(all_worklist, ";"); // Parse new request list (ex. all_worklist = ";13;52;1;32")

        subjectListSet = new HashSet<String>();

        while (st.hasMoreTokens()) {
            String id_str = st.nextToken();
            int id = Integer.parseInt(id_str);
            Log.d(TAG, "id: " + id_str);

            JSONObject request = RequestManager.getRequestInfo(Session.GetInstance(), id);

            String subjectList = (String) request.get("subject");
            Log.d(TAG, "subject: " + subjectList);
            setSpinnerSet(subjectListSet, subjectList);
        }

        if (subjectListSet != null) {
            return subjectListSet;
        } else {
            Log.e(TAG, "subjectListSet is null");
            return null;
        }
    }

    private static void setSpinnerSet(Set<String> key, String val) {

        if (key != null && !val.isEmpty()) {
            key.add(val);
            Log.d(TAG, "Add to Set: " + val);
        }
    }

    @TargetApi(Build.VERSION_CODES.DONUT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        mContext = this;
        mActivity = this;
        super.onCreate(savedInstanceState);

        if (mMyprofile == null) {
            Log.e(TAG, "No response from ProfileManager.getMyInfo");
        }

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
                if (msg.what == UPLOADED) {
                    Log.d(TAG, "msg: UPLAODED");
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                } else if (msg.what == TIME_OUT || msg.what == ERROR) {
                    Log.d(TAG, "msg: error, code: " + msg.what);
                    if (dialog != null) {
                        Toast.makeText(mContext, "Error.. uploading file", Toast.LENGTH_SHORT);
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
            if (requestCode == PICK_FILE_REQUEST1 || requestCode == PICK_FILE_REQUEST2) {
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
                if (requestCode == PICK_FILE_REQUEST2) {
                    Log.d(TAG, "Upload file directly");
                    uploadFileDirect(upload_file_request_id, upload_file_column, null);
                }

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
        static HashSet<String> all_doc_path = null;
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
                TextView tview = (TextView) view.findViewById(R.id.req_list_item_desc);
                if (showEvaluationBtn(view, groupPosition)) {
                    tview.setLayoutParams(new LinearLayout.LayoutParams(300, 40));
                } else {
                    tview.setLayoutParams(new LinearLayout.LayoutParams
                            (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                }
                return view;
            }
            checkApplyBtn(view, groupPosition);
            return view;
        }

        private boolean showEvaluationBtn(View view, int groupPosition) {
            final Button evaluate_btn = (Button) view.findViewById(R.id.request_evaluation_btn);

            Map<String, String> groupdata = (Map<String, String>) super.getGroup(groupPosition);
            if (groupdata.get("IS_FINISHED") == null) {
                disableButton(evaluate_btn, "Evaluate");
                evaluate_btn.setVisibility(View.GONE);
                return false;
            }

            evaluate_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "evaluate_btn onClick");
                    // TODO: Evaluate
                    //new Notifier(Notifier.Command.RESULT_OF_BID, request_id, mContext);
                }
            });
            evaluate_btn.setVisibility(View.VISIBLE);
            enableButton(evaluate_btn, "Evaluate");
            return true;
        }

        private void checkApplyBtn(View view, int groupPosition) {
            // Show Apply Button If it's new requests (Not applied yet)
            // Only for Translator and Reviewer
            Map<String, String> groupdata = (Map<String, String>) super.getGroup(groupPosition);
            final Button apply_btn = (Button) view.findViewById(R.id.request_apply_btn);
            if (groupdata.get("IS_APPLIED").equals("TRUE")) {
                //Log.d(TAG, "IS_APPLIED = TRUE");
                if (groupdata.get("IS_EMPLOYED").equals("TRUE"))
                    disableButton(apply_btn, "Employed");
                else
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
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                                 View convertView, ViewGroup parent) {
            View view = super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);

            // Show Select Button If it's candidates list
            // Only for Requester and Translator
            Map<String, String> childdata = (Map<String, String>) super.getChild(groupPosition, childPosition);
            CheckSelectBtn(view, childdata, groupPosition, childPosition);
            CheckDownloadBtn(view, childdata, groupPosition, childPosition);
            CheckUploadBtn(view, childdata, groupPosition, childPosition);
            return view;
        }

        private String getAnotherChildData(String target, int groupPosition) {
            List<Map<String, String>> children = (List<Map<String, String>>) mChildData.get(groupPosition);
            Iterator<Map<String, String>> itr = children.iterator();
            while (itr.hasNext()) {
                Map<String, String> key = itr.next();
                if (key.get("KEY").equals(target)) {
                    return key.get("DATA");
                }
            }
            return null;
        }

        private void CheckSelectBtn(View view, Map<String, String> childdata, int groupPosition, int childPosition) {
            final Button select_btn = (Button) view.findViewById(R.id.select_worker_btn);
            String target, target2;
            if (mUserMode == ProfileManager.USER_MODE.REQUESTER) {
                target = "translator_candidate_list";
                target2 = "translator_id";
            } else if (mUserMode == ProfileManager.USER_MODE.TRANSLATOR) {// if current user is translator
                target = "reviewer_candidate_list";
                target2 = "reviewer_id";
                Map<String, String> groupdata = (Map<String, String>) super.getGroup(groupPosition);
                String is_employed = groupdata.get("IS_EMPLOYED");
                if (is_employed == null || is_employed.equals("FALSE")) {
                    select_btn.setVisibility(View.GONE);
                    return;
                }
            } else {
                select_btn.setVisibility(View.GONE);
                return;
            }

            if (false == childdata.get("KEY").equals(target) || childdata.get("DATA").isEmpty()) {
                select_btn.setVisibility(View.GONE);
            } else {
                // If this group has worker_id , disable Button;
                /*
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
                */

                String worker_id = getAnotherChildData(target2, groupPosition);
                if (worker_id != null && false == worker_id.isEmpty()) {
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
                            intent.putExtra("candidates_list", (String) view.getTag(R.id.select_worker_btn));
                            intent.putExtra("request_id", (String) view.getTag());
                            intent.setClassName(AccessManager.LTS_PACKAGE_NAME, AccessManager.SELWKR_CLASS_NAME);
                            mContext.startActivity(intent);
                        }
                    });
                    select_btn.setVisibility(View.VISIBLE);
                }
            }
        }

        @TargetApi(Build.VERSION_CODES.M)
        private void CheckDownloadBtn(View view, Map<String, String> childdata, int groupPosition, int childPosition) {
            final Button download_btn = (Button) view.findViewById(R.id.download_btn);

            String worker_id = null;
            if (mUserMode == ProfileManager.USER_MODE.REQUESTER) {
                worker_id = getAnotherChildData("requester_id", groupPosition);
            } else if (mUserMode == ProfileManager.USER_MODE.TRANSLATOR) {// if current user is translator
                worker_id = getAnotherChildData("translator_id", groupPosition);
            } else {
                worker_id = getAnotherChildData("reviewer_id", groupPosition);
            }

            String myid = (mMyprofile != null ? (String) (mMyprofile.get("id")) : null);
            if (worker_id == null || myid == null || worker_id.equals(myid) == false) { // TODO: Only If I involved to this work.
                download_btn.setVisibility(View.GONE);
                return;
            }

            if (all_doc_path == null) {
                all_doc_path = new HashSet<String>();
                all_doc_path.add("source_doc_path");
                all_doc_path.add("translated_doc_path");
                all_doc_path.add("reviewed_doc_path");
                all_doc_path.add("final_doc_path");
            }

            if (false == all_doc_path.contains(childdata.get("KEY"))
                    || true == childdata.get("DATA").isEmpty()) {
                download_btn.setVisibility(View.GONE);
                return;
            }

            enableButton(download_btn, "Download");
            download_btn.setTag(childdata.get("DATA")); // Download path
            download_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Downlad this path(= (String)view.getTag());
                    Log.d(TAG, "Download onClick");
                    String fileName = (String) view.getTag();
                    if (fileName == null || fileName.isEmpty()) {
                        Toast.makeText(mContext, "Please select the document", Toast.LENGTH_SHORT);
                        return;
                    }
                    FileHandler.downloadFile(mContext, fileName);
                }
            });
            download_btn.setVisibility(View.VISIBLE);
        }

        @TargetApi(Build.VERSION_CODES.M)
        private void CheckUploadBtn(View view, Map<String, String> childdata, int groupPosition, int childPosition) {
            final Button upload_btn = (Button) view.findViewById(R.id.upload_btn);

            String worker_id = null;
            if (mUserMode == ProfileManager.USER_MODE.REQUESTER) {
                worker_id = getAnotherChildData("requester_id", groupPosition);
            } else if (mUserMode == ProfileManager.USER_MODE.TRANSLATOR) {// if current user is translator
                worker_id = getAnotherChildData("translator_id", groupPosition);
            } else {
                worker_id = getAnotherChildData("reviewer_id", groupPosition);
            }

            String myid = (mMyprofile != null ? (String) (mMyprofile.get("id")) : null);
            if (worker_id == null || myid == null || worker_id.equals(myid) == false) { // TODO: Only If I involved to this work.
                upload_btn.setVisibility(View.GONE);
                return;
            }

            if (all_doc_path == null) {
                all_doc_path = new HashSet<String>();
                all_doc_path.add("source_doc_path");
                all_doc_path.add("translated_doc_path");
                all_doc_path.add("reviewed_doc_path");
                all_doc_path.add("final_doc_path");
            }

            if (false == all_doc_path.contains(childdata.get("KEY"))
                    || false == childdata.get("DATA").isEmpty()) {
                upload_btn.setVisibility(View.GONE);
                return;
            }

            switch (mUserMode) {
                case ProfileManager.USER_MODE.REQUESTER:
                    if (false == childdata.get("KEY").equals("source_doc_path"))
                        return;
                    break;
                case ProfileManager.USER_MODE.TRANSLATOR:
                    if (false == childdata.get("KEY").equals("translated_doc_path")
                            && false == childdata.get("KEY").equals("final_doc_path"))
                        return;
                    break;
                case ProfileManager.USER_MODE.REVIEWER:
                    if (false == childdata.get("KEY").equals("reviewed_doc_path"))
                        return;
                    break;
                default:
                    break;
            }

            enableButton(upload_btn, "Upload");
            Map<String, String> groupdata = (Map<String, String>) super.getGroup(groupPosition);

            String request_id = groupdata.get("ID");
            Log.d(TAG, "getAnotherChildData request_id:" + request_id);
            upload_btn.setTag(R.id.upload_btn, request_id); // request_id
            upload_btn.setTag(R.id.upload_btn + 1, childdata.get("KEY")); // column
            upload_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "Upload onClick");
                    fh = new FileHandler();
                    upload_btn.setVisibility(View.GONE);
                    String request_id = (String) view.getTag(R.id.upload_btn);
                    String column = (String) view.getTag(R.id.upload_btn + 1);
                    showFileChooser(mActivity, PICK_FILE_REQUEST2, request_id, column);
                }
            });
            upload_btn.setVisibility(View.VISIBLE);
        }

        private void disableButton(final Button btn, String text) {
            btn.setText(text);
            btn.setOnClickListener(null);
            btn.setClickable(false);
            if (text.equals("Employed"))
                btn.setBackgroundColor(0xFF5167D6);
            else
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
            displayRequestItems(view, ac);

            createSpinners(view);
            createDateSelector(view);

            if (ProfileManager.user_mode == -1) {
                ProfileManager.getUserMode(Session.GetInstance());
            }

            switch (ProfileManager.user_mode) {
                case ProfileManager.USER_MODE.REQUESTER:
                    type.setVisibility(View.VISIBLE);
                    level.setVisibility(View.VISIBLE);
                    payment.setVisibility(View.VISIBLE);
                    lang.setVisibility(View.VISIBLE);

                    createUploadItems(view, ac);
                    break;
                case ProfileManager.USER_MODE.TRANSLATOR:
                case ProfileManager.USER_MODE.REVIEWER:
                    break;
                default:
                    break;
            }

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
                        String fileName = (String) file.getSelectedItem();

                        if (fileName == null || fileName.isEmpty()) {
                            Toast.makeText(mContext, "Please select the document", Toast.LENGTH_SHORT);
                            return;
                        }

                        FileHandler.downloadFile(mContext, fileName);
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
                                    dueDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
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

            if (mMyprofile == null)
                mMyprofile = ProfileManager.getMyInfo(Session.GetInstance());
            View rootView = inflater.inflate(R.layout.fragment_client, container, false);
            int pageViewNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            int user_mode = ProfileManager.getUserMode(Session.GetInstance());
            switch (pageViewNumber) {
                case PAGE_NUM_NOTIFY:
                    showWorkList(getActivity(), rootView, user_mode);
                    break;
                case PAGE_NUM_REQUEST:
                    showRequestItems(getActivity(), rootView);
                    break;
                case PAGE_NUM_PROFILE:
                    showMyProfile(getActivity(), rootView);
                default:
            }
            //TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            //textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

            return rootView;
        }

        private void showMyProfile(Activity ac, View v) {
            ScrollView profileLayout = (ScrollView) v.findViewById(R.id.profile_layout);
            profileLayout.setVisibility(View.VISIBLE);


            //String myid = (mMyprofile != null ? (String)(mMyprofile.get("id")) : null);

            EditText idEdit = (EditText) v.findViewById(R.id.et_id);
            idEdit.setText((String) mMyprofile.get("id"));

            EditText givenNameEdit = (EditText) v.findViewById(R.id.et_names);
            givenNameEdit.setText((String) mMyprofile.get("family_name"));

            EditText surNameEdit = (EditText) v.findViewById(R.id.et_surname);
            surNameEdit.setText((String) mMyprofile.get("first_name"));

            EditText emailEdit = (EditText) v.findViewById(R.id.et_email);
            emailEdit.setText((String) mMyprofile.get("email"));

            EditText phoneEdit = (EditText) v.findViewById(R.id.et_phone);
            phoneEdit.setText((String) mMyprofile.get("phone"));

            EditText countryEdit = (EditText) v.findViewById(R.id.et_country);
            countryEdit.setText((String) mMyprofile.get("country"));

            EditText addressEdit = (EditText) v.findViewById(R.id.et_address);
            addressEdit.setText((String) mMyprofile.get("address"));

            String sex = (String) mMyprofile.get("sex");

            CheckBox ckBox;

            if (sex.equals("1")) {
                ckBox = (CheckBox) v.findViewById(R.id.btn_woman);
                ckBox.setChecked(true);
            } else if (sex.equals("0")) {
                ckBox = (CheckBox) v.findViewById(R.id.btn_man);
                ckBox.setChecked(true);
            }
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
